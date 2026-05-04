package com.poketeambuilder.services.seed;

import com.poketeambuilder.entities.Nature;

import com.poketeambuilder.dtos.front.admin.seed.SeedResult;

import com.poketeambuilder.dtos.pokeapi.nature.NatureApiDto;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.poketeambuilder.mappers.implementation.NatureMapper;

import com.poketeambuilder.repositories.NatureRepository;

import com.poketeambuilder.services.command.PokeApiClient;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NatureSeedService {

    private static final Logger log = LoggerFactory.getLogger(NatureSeedService.class);

    private final NatureMapper natureMapper;
    private final NatureRepository natureRepository;

    private final PokeApiClient pokeApiClient;

    private static final String NATURE_ENDPOINT = "/nature";

    @Transactional
    public SeedResult seed() {
        int errors = 0;

        List<PokeApiResource> resources = pokeApiClient.fetchAllResources(NATURE_ENDPOINT);

        List<NatureApiDto> apiDtos = new ArrayList<>();

        for (PokeApiResource resource : resources) {
            try {
                NatureApiDto apiDto = pokeApiClient.fetchResource(resource.url(), NatureApiDto.class);

                apiDtos.add(apiDto);
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch nature resource: {}", resource.url(), e);
            }
        }

        List<Nature> entities = apiDtos.stream()
            .map(natureMapper::toEntity)
            .toList();

        natureRepository.saveAll(entities);

        log.info("Seeded {} natures ({} fetch errors)", entities.size(), errors);

        return SeedResult.of(entities.size(), errors);
    }

    @Transactional
    public void clearSeedData() {
        natureRepository.deleteAllInBatch();
    }
}