package com.poketeambuilder.services.seed;

import java.util.List;
import java.util.ArrayList;

import com.poketeambuilder.entities.Ability;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.dtos.pokeapi.ability.AbilityApiDto;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.poketeambuilder.mappers.implementation.AbilityMapper;

import com.poketeambuilder.repositories.AbilityRepository;

import com.poketeambuilder.services.command.PokeApiClient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Seeds the {@code ability} table from PokeAPI. Filters out non-canon ability ids before
 * fetching individual resources.
 */
@Slf4j
@Service
public class AbilitySeedService {

    private static final String ABILITY_ENDPOINT = "/ability";
    private static final int NON_CANON_ABILITY_ID_THRESHOLD = 10000;

    private final AbilityMapper abilityMapper;
    private final AbilityRepository abilityRepository;

    private final PokeApiClient pokeApiClient;
    private final TransactionTemplate transactionTemplate;

    public AbilitySeedService(AbilityMapper abilityMapper, AbilityRepository abilityRepository, PokeApiClient pokeApiClient, TransactionTemplate transactionTemplate) {
        this.abilityMapper = abilityMapper;
        this.pokeApiClient = pokeApiClient;
        this.abilityRepository = abilityRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public SeedResultDto seed() {
        FetchResult fetched = fetchAll();
        int saved = transactionTemplate.execute(status -> persist(fetched.apiDtos()));
        log.info("Seeded {} abilities ({} fetch errors)", saved, fetched.errors());
        return SeedResultDto.of(saved, fetched.errors());
    }

    @Transactional
    public void clearSeedData() {
        abilityRepository.deleteAllInBatch();
    }

    private FetchResult fetchAll() {
        List<PokeApiResource> resources = pokeApiClient.fetchAllResources(ABILITY_ENDPOINT).stream()
                .filter(resource -> {
                    Integer id = resource.extractId();
                    return id != null && id <= NON_CANON_ABILITY_ID_THRESHOLD;
                })
                .toList();

        List<AbilityApiDto> apiDtos = new ArrayList<>();
        int errors = 0;

        for (PokeApiResource resource : resources) {
            try {
                apiDtos.add(pokeApiClient.fetchResource(resource.url(), AbilityApiDto.class));
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch ability resource: {}", resource.url(), e);
            }
        }

        return new FetchResult(apiDtos, errors);
    }

    private int persist(List<AbilityApiDto> apiDtos) {
        List<Ability> entities = apiDtos.stream().map(abilityMapper::toEntity).toList();
        abilityRepository.saveAll(entities);
        return entities.size();
    }

    private record FetchResult(List<AbilityApiDto> apiDtos, int errors) {}
}
