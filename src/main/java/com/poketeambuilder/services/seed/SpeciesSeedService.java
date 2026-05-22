package com.poketeambuilder.services.seed;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map.Entry;

import com.poketeambuilder.entities.PokemonSpecies;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.evolution.ChainLinkApiDto;
import com.poketeambuilder.dtos.pokeapi.species.PokemonSpeciesApiDto;
import com.poketeambuilder.dtos.pokeapi.evolution.EvolutionChainApiDto;
import com.poketeambuilder.dtos.pokeapi.evolution.EvolutionDetailApiDto;

import com.poketeambuilder.mappers.implementation.SpeciesMapper;

import com.poketeambuilder.repositories.SpeciesRepository;

import com.poketeambuilder.services.command.PokeApiClient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Seeds the {@code pokemon_species} table from PokeAPI. Runs in three steps so HTTP and DB
 * work never overlap:
 *
 * <ol>
 *   <li>Fetch all species DTOs.</li>
 *   <li>Persist species rows.</li>
 *   <li>Fetch every distinct evolution chain, then persist the resolved {@code previous_evolution} links and {@code evolution_*}.</li>
 * </ol>
 */
@Slf4j
@Service
public class SpeciesSeedService {

    private static final String SPECIES_ENDPOINT = "/pokemon-species";

    private final SpeciesMapper speciesMapper;
    private final SpeciesRepository speciesRepository;

    private final PokeApiClient pokeApiClient;
    private final TransactionTemplate transactionTemplate;

    public SpeciesSeedService(SpeciesMapper speciesMapper, SpeciesRepository speciesRepository,
                              PokeApiClient pokeApiClient, TransactionTemplate transactionTemplate) {
        this.speciesMapper = speciesMapper;
        this.speciesRepository = speciesRepository;
        this.pokeApiClient = pokeApiClient;
        this.transactionTemplate = transactionTemplate;
    }

    public SeedResultDto seed() {
        FetchResult fetched = fetchAllSpecies();
        int saved = transactionTemplate.execute(status -> persistSpecies(fetched.apiDtos()));
        log.info("Seeded {} species ({} fetch errors)", saved, fetched.errors());

        Map<String, EvolutionChainApiDto> chainsByUrl = fetchEvolutionChains(fetched.apiDtos());
        int linked = transactionTemplate.execute(status -> linkEvolutions(fetched.apiDtos(), chainsByUrl));
        log.info("Linked {} evolution references", linked);

        return SeedResultDto.of(saved, fetched.errors());
    }

    @Transactional
    public void clearSeedData() {
        speciesRepository.clearPreviousEvolutions();
        speciesRepository.deleteAllInBatch();
    }

    private FetchResult fetchAllSpecies() {
        List<PokeApiResource> resources = pokeApiClient.fetchAllResources(SPECIES_ENDPOINT);

        List<PokemonSpeciesApiDto> apiDtos = new ArrayList<>();
        int errors = 0;

        for (PokeApiResource resource : resources) {
            try {
                apiDtos.add(pokeApiClient.fetchResource(resource.url(), PokemonSpeciesApiDto.class));
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch species resource: {}", resource.url(), e);
            }
        }

        return new FetchResult(apiDtos, errors);
    }

    private int persistSpecies(List<PokemonSpeciesApiDto> apiDtos) {
        List<PokemonSpecies> entities = apiDtos.stream().map(speciesMapper::toEntity).toList();
        speciesRepository.saveAllAndFlush(entities);
        return entities.size();
    }

    /** Fetches every distinct evolution-chain resource referenced by the species. */
    private Map<String, EvolutionChainApiDto> fetchEvolutionChains(List<PokemonSpeciesApiDto> speciesDtos) {
        Set<String> chainUrls = new HashSet<>();
        for (PokemonSpeciesApiDto dto : speciesDtos) {
            if (dto.evolutionChain() != null && dto.evolutionChain().url() != null) {
                chainUrls.add(dto.evolutionChain().url());
            }
        }

        Map<String, EvolutionChainApiDto> chainsByUrl = new HashMap<>();
        for (String chainUrl : chainUrls) {
            try {
                chainsByUrl.put(chainUrl, pokeApiClient.fetchResource(chainUrl, EvolutionChainApiDto.class));
            } catch (Exception e) {
                log.error("Failed to fetch evolution chain: {}", chainUrl, e);
            }
        }
        return chainsByUrl;
    }

    /**
     * Applies previous-evolution links and evolution-detail fields.
     */
    private int linkEvolutions(List<PokemonSpeciesApiDto> speciesDtos, Map<String, EvolutionChainApiDto> chainsByUrl) {
        Map<Integer, Integer> previousEvolutionLinks = buildEvolutionLinks(speciesDtos);
        applyPreviousEvolutions(previousEvolutionLinks);

        Map<Integer, EvolutionDetailApiDto> detailsBySpeciesId = collectEvolutionDetails(chainsByUrl);
        applyEvolutionDetails(detailsBySpeciesId);

        speciesRepository.flush();

        log.info("Applied evolution details to {} species", detailsBySpeciesId.size());

        return previousEvolutionLinks.size();
    }

    private Map<Integer, Integer> buildEvolutionLinks(List<PokemonSpeciesApiDto> speciesDtos) {
        Map<Integer, Integer> links = new HashMap<>();

        for (PokemonSpeciesApiDto dto : speciesDtos) {
            if (dto.evolvesFromSpecies() == null) {
                continue;
            }

            Integer preevolutionId = dto.evolvesFromSpecies().extractId();
            if (preevolutionId != null) {
                links.put(dto.id(), preevolutionId);
            }
        }

        return links;
    }

    private void applyPreviousEvolutions(Map<Integer, Integer> evolutionLinks) {
        for (Entry<Integer, Integer> link : evolutionLinks.entrySet()) {
            try {
                PokemonSpecies evolution = speciesRepository.findById(link.getKey())
                        .orElseThrow(() -> new IllegalStateException("Species not found: " + link.getKey()));
                PokemonSpecies preevolution = speciesRepository.getReferenceById(link.getValue());
                evolution.setPreviousEvolution(preevolution);
            } catch (Exception e) {
                log.error("Failed to link previous evolution {} -> {}", link.getKey(), link.getValue(), e);
            }
        }
    }

    private Map<Integer, EvolutionDetailApiDto> collectEvolutionDetails(Map<String, EvolutionChainApiDto> chainsByUrl) {
        Map<Integer, EvolutionDetailApiDto> detailsBySpeciesId = new HashMap<>();
        for (EvolutionChainApiDto chain : chainsByUrl.values()) {
            if (chain.chain() != null) {
                walkChain(chain.chain(), detailsBySpeciesId);
            }
        }
        return detailsBySpeciesId;
    }

    private void applyEvolutionDetails(Map<Integer, EvolutionDetailApiDto> detailsBySpeciesId) {
        for (Entry<Integer, EvolutionDetailApiDto> entry : detailsBySpeciesId.entrySet()) {
            try {
                PokemonSpecies species = speciesRepository.findById(entry.getKey())
                        .orElseThrow(() -> new IllegalStateException("Species not found: " + entry.getKey()));
                applyEvolutionDetail(species, entry.getValue());
            } catch (Exception e) {
                log.error("Failed to apply evolution details for species {}", entry.getKey(), e);
            }
        }
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

    private void applyEvolutionDetail(PokemonSpecies species, EvolutionDetailApiDto detail) {
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

    private record FetchResult(List<PokemonSpeciesApiDto> apiDtos, int errors) {}
}
