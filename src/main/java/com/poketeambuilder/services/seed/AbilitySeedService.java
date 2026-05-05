package com.poketeambuilder.services.seed;

import com.poketeambuilder.entities.Ability;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.dtos.pokeapi.ability.AbilityApiDto;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.poketeambuilder.mappers.implementation.AbilityMapper;

import com.poketeambuilder.repositories.AbilityRepository;

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
public class AbilitySeedService {
    
    private final static Logger log = LoggerFactory.getLogger(AbilitySeedService.class);

    private final AbilityMapper abilityMapper;
    private final AbilityRepository abilityRepository;

    private final PokeApiClient pokeApiClient;

    private static final String ABILITY_ENDPOINT = "/ability";
    private static final int NON_CANNON_ABILITY_ID_THRESHOLD = 10000;

    @Transactional
    public SeedResultDto seed() {
        int errors = 0;

        List<PokeApiResource> resources = pokeApiClient.fetchAllResources(ABILITY_ENDPOINT)
            .stream()
            .filter(resource -> {
                Integer id = resource.extractId();
                return id != null && id <= NON_CANNON_ABILITY_ID_THRESHOLD;
            })
            .toList();

        List<AbilityApiDto> apiDtos = new ArrayList<>();

        for (PokeApiResource resource : resources) {
            try {
                AbilityApiDto apiDto = pokeApiClient.fetchResource(resource.url(), AbilityApiDto.class);

                apiDtos.add(apiDto);
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch ability resource: {}", resource.url(), e);
            }
        }

        List<Ability> entities = apiDtos.stream()
            .map(abilityMapper::toEntity)
            .toList();

        abilityRepository.saveAll(entities);

        log.info("Seeded {} abilities ({} fetch errors)", entities.size(), errors);

        return SeedResultDto.of(entities.size(), errors);
    }

    @Transactional
    public void clearSeedData() {
        abilityRepository.deleteAllInBatch();
    }
}
