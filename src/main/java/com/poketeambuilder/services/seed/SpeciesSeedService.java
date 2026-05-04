package com.poketeambuilder.services.seed;

import com.poketeambuilder.entities.PokemonSpecies;

import com.poketeambuilder.dtos.front.admin.seed.SeedResult;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.evolution.ChainLinkApiDto;
import com.poketeambuilder.dtos.pokeapi.species.PokemonSpeciesApiDto;
import com.poketeambuilder.dtos.pokeapi.evolution.EvolutionChainApiDto;
import com.poketeambuilder.dtos.pokeapi.evolution.EvolutionDetailApiDto;

import com.poketeambuilder.mappers.implementation.SpeciesMapper;

import com.poketeambuilder.repositories.SpeciesRepository;

import com.poketeambuilder.services.command.PokeApiClient;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpeciesSeedService {

    private static final Logger log = LoggerFactory.getLogger(SpeciesSeedService.class);

    private final SpeciesMapper speciesMapper;
    private final SpeciesRepository speciesRepository;

    private final PokeApiClient pokeApiClient;

    private static final String SPECIES_ENDPOINT = "/pokemon-species";

    @Transactional
    public SeedResult seed() {
        int errors = 0;

        List<PokeApiResource> resources = pokeApiClient.fetchAllResources(SPECIES_ENDPOINT);

        List<PokemonSpeciesApiDto> speciesDtos = new ArrayList<>();

        for (PokeApiResource resource : resources) {
            try {
                PokemonSpeciesApiDto dto = pokeApiClient.fetchResource(resource.url(), PokemonSpeciesApiDto.class);
                speciesDtos.add(dto);
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch species resource: {}", resource.url(), e);
            }
        }

        List<PokemonSpecies> entities = speciesDtos.stream()
            .map(speciesMapper::toEntity)
            .toList();

        speciesRepository.saveAllAndFlush(entities);

        log.info("Seeded {} species ({} fetch errors)", entities.size(), errors);

        Map<Integer, Integer> evolutionLinks = buildEvolutionLinks(speciesDtos);

        linkPreviousEvolutions(evolutionLinks);

        log.info("Linked {} evolution references", evolutionLinks.size());

        int detailsApplied = seedEvolutionDetails(speciesDtos);

        log.info("Applied evolution details to {} species", detailsApplied);

        return new SeedResult(entities.size(), errors);
    }

    @Transactional
    public void clearSeedData() {
        speciesRepository.clearPreviousEvolutions();
        speciesRepository.deleteAllInBatch();
    }

    private Map<Integer, Integer> buildEvolutionLinks(List<PokemonSpeciesApiDto> speciesDtos) {
        Map<Integer, Integer> links = new HashMap<>();

        for (PokemonSpeciesApiDto dto : speciesDtos) {
            if (dto.evolvesFromSpecies() != null) {
                Integer preevolutionId = dto.evolvesFromSpecies().extractId();

                if (preevolutionId != null) {
                    links.put(dto.id(), preevolutionId);
                }
            }
        }

        return links;
    }

    private void linkPreviousEvolutions(Map<Integer, Integer> evolutionLinks) {
        for (Entry<Integer, Integer> link : evolutionLinks.entrySet()) {
            PokemonSpecies evolution = speciesRepository.getReferenceById(link.getKey());
            PokemonSpecies preevolution = speciesRepository.getReferenceById(link.getValue());

            evolution.setPreviousEvolution(preevolution);
        }

        speciesRepository.flush();
    }

    private int seedEvolutionDetails(List<PokemonSpeciesApiDto> speciesDtos) {
        Set<String> chainUrls = collectUniqueChainUrls(speciesDtos);

        Map<Integer, EvolutionDetailApiDto> detailsBySpeciesId = new HashMap<>();

        for (String chainUrl : chainUrls) {
            try {
                EvolutionChainApiDto chain = pokeApiClient.fetchResource(chainUrl, EvolutionChainApiDto.class);

                if (chain.chain() != null) {
                    walkChain(chain.chain(), detailsBySpeciesId);
                }
            } catch (Exception e) {
                log.error("Failed to fetch evolution chain: {}", chainUrl, e);
            }
        }

        for (Entry<Integer, EvolutionDetailApiDto> entry : detailsBySpeciesId.entrySet()) {
            try {
                PokemonSpecies species = speciesRepository.getReferenceById(entry.getKey());

                applyEvolutionDetails(species, entry.getValue());
            } catch (Exception e) {
                log.error("Failed to apply evolution details for species {}", entry.getKey(), e);
            }
        }

        speciesRepository.flush();

        return detailsBySpeciesId.size();
    }

    private Set<String> collectUniqueChainUrls(List<PokemonSpeciesApiDto> speciesDtos) {
        Set<String> urls = new HashSet<>();

        for (PokemonSpeciesApiDto dto : speciesDtos) {
            if (dto.evolutionChain() != null && dto.evolutionChain().url() != null) {
                urls.add(dto.evolutionChain().url());
            }
        }

        return urls;
    }

    private void walkChain(ChainLinkApiDto link, Map<Integer, EvolutionDetailApiDto> detailsBySpeciesId) {
        if (link.evolutionDetails() != null && !link.evolutionDetails().isEmpty() && link.species() != null) {
            Integer speciesId = link.species().extractId();

            if (speciesId != null) {
                detailsBySpeciesId.put(speciesId, link.evolutionDetails().get(0));
            }
        }

        if (link.evolvesTo() != null) {
            for (ChainLinkApiDto child : link.evolvesTo()) {
                walkChain(child, detailsBySpeciesId);
            }
        }
    }

    private void applyEvolutionDetails(PokemonSpecies species, EvolutionDetailApiDto detail) {
        if (detail.trigger() != null) {
            species.setEvolutionTrigger(detail.trigger().name());
        }
        if (detail.item() != null) {
            species.setEvolutionItem(detail.item().name());
        }
        if (detail.heldItem() != null) {
            species.setEvolutionHeldItem(detail.heldItem().name());
        }

        species.setEvolutionMinLevel(detail.minLevel());
        species.setEvolutionMinHappiness(detail.minHappiness());

        if (detail.timeOfDay() != null && !detail.timeOfDay().isBlank()) {
            species.setEvolutionTimeOfDay(detail.timeOfDay());
        }
    }
}