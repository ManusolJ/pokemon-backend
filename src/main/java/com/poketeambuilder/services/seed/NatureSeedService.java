package com.poketeambuilder.services.seed;

import java.util.List;
import java.util.ArrayList;

import com.poketeambuilder.entities.Nature;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.dtos.pokeapi.nature.NatureApiDto;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.poketeambuilder.mappers.implementation.NatureMapper;

import com.poketeambuilder.repositories.NatureRepository;

import com.poketeambuilder.services.command.PokeApiClient;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Seeds the {@code nature} table from PokeAPI.
 */
@Slf4j
@Service
public class NatureSeedService {

    private static final String NATURE_ENDPOINT = "/nature";

    private final NatureMapper natureMapper;
    private final NatureRepository natureRepository;

    private final PokeApiClient pokeApiClient;
    private final TransactionTemplate transactionTemplate;

    public NatureSeedService(NatureMapper natureMapper, NatureRepository natureRepository,PokeApiClient pokeApiClient, TransactionTemplate transactionTemplate) {
        this.natureMapper = natureMapper;
        this.pokeApiClient = pokeApiClient;
        this.natureRepository = natureRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public SeedResultDto seed() {
        FetchResult fetched = fetchAll();
        int saved = transactionTemplate.execute(status -> persist(fetched.apiDtos()));
        log.info("Seeded {} natures ({} fetch errors)", saved, fetched.errors());
        return SeedResultDto.of(saved, fetched.errors());
    }

    @Transactional
    public void clearSeedData() {
        natureRepository.deleteAllInBatch();
    }

    private FetchResult fetchAll() {
        List<PokeApiResource> resources = pokeApiClient.fetchAllResources(NATURE_ENDPOINT);

        List<NatureApiDto> apiDtos = new ArrayList<>();
        int errors = 0;

        for (PokeApiResource resource : resources) {
            try {
                apiDtos.add(pokeApiClient.fetchResource(resource.url(), NatureApiDto.class));
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch nature resource: {}", resource.url(), e);
            }
        }

        return new FetchResult(apiDtos, errors);
    }

    private int persist(List<NatureApiDto> apiDtos) {
        List<Nature> entities = apiDtos.stream().map(natureMapper::toEntity).toList();
        natureRepository.saveAll(entities);
        return entities.size();
    }

    private record FetchResult(List<NatureApiDto> apiDtos, int errors) {}
}
