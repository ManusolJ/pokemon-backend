package com.poketeambuilder.services.seed;

import com.poketeambuilder.entities.Type;
import com.poketeambuilder.entities.TypeEffectiveness;

import com.poketeambuilder.dtos.front.admin.seed.SeedResult;

import com.poketeambuilder.dtos.pokeapi.type.TypeApiDto;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.poketeambuilder.mappers.implementation.TypeMapper;
import com.poketeambuilder.mappers.helpers.resource.TypeIngestionHelper;

import com.poketeambuilder.repositories.TypeRepository;
import com.poketeambuilder.repositories.TypeEffectivenessRepository;

import com.poketeambuilder.services.command.PokeApiClient;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TypeSeedService {

    private static final Logger log = LoggerFactory.getLogger(TypeSeedService.class);

    private final TypeMapper typeMapper;
    private final TypeRepository typeRepository;
    private final TypeIngestionHelper typeIngestionHelper;
    private final TypeEffectivenessRepository typeEffectivenessRepository;

    private final PokeApiClient pokeApiClient;

    private static final int MAX_CANON_TYPE_ID = 18;
    
    private static final String TYPE_ENDPOINT = "/type";

    private static final BigDecimal MULTIPLIER_IMMUNE = BigDecimal.ZERO;
    private static final BigDecimal MULTIPLIER_HALF = new BigDecimal("0.50");
    private static final BigDecimal MULTIPLIER_NEUTRAL = new BigDecimal("1.00");
    private static final BigDecimal MULTIPLIER_DOUBLE = new BigDecimal("2.00");

    @Transactional
    public SeedResult seed() {
        int errors = 0;

        List<PokeApiResource> resources = pokeApiClient.fetchAllResources(TYPE_ENDPOINT);

        List<TypeApiDto> typeDtos = new ArrayList<>();

        for (PokeApiResource resource : resources) {
            try {
                TypeApiDto dto = pokeApiClient.fetchResource(resource.url(), TypeApiDto.class);

                if (dto.id() <= MAX_CANON_TYPE_ID) {
                    typeDtos.add(dto);
                }
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch type resource: {}", resource.url(), e);
            }
        }

        List<Type> types = typeDtos.stream()
            .map(typeMapper::toEntity)
            .toList();

        typeEffectivenessRepository.deleteAll();
        typeRepository.deleteAll();

        typeRepository.saveAll(types);

        log.info("Seeded {} types ({} fetch errors)", types.size(), errors);

        int effectivenessCount = seedTypeEffectiveness(typeDtos);

        log.info("Seeded {} type effectiveness entries", effectivenessCount);

        return SeedResult.of(types.size() + effectivenessCount, errors);
    }

    private int seedTypeEffectiveness(List<TypeApiDto> typeDtos) {
        Set<Integer> allTypeIds = typeDtos.stream()
            .map(TypeApiDto::id)
            .collect(Collectors.toSet());

        List<TypeEffectiveness> entries = new ArrayList<>();

        for (TypeApiDto dto : typeDtos) {
            Type attacker = typeRepository.getReferenceById(dto.id());

            Map<Integer, BigDecimal> multipliers = buildMultiplierMap(dto, allTypeIds);

            for (Entry<Integer, BigDecimal> entry : multipliers.entrySet()) {
                Type defender = typeRepository.getReferenceById(entry.getKey());

                entries.add(TypeEffectiveness.builder()
                    .attackingType(attacker)
                    .defendingType(defender)
                    .multiplier(entry.getValue())
                    .build());
            }
        }

        typeEffectivenessRepository.saveAll(entries);

        return entries.size();
    }

    private Map<Integer, BigDecimal> buildMultiplierMap(TypeApiDto dto, Set<Integer> allTypeIds) {
        Map<Integer, BigDecimal> multipliers = new HashMap<>();

        for (Integer typeId : allTypeIds) {
            multipliers.put(typeId, MULTIPLIER_NEUTRAL);
        }

        for (PokeApiResource target : typeIngestionHelper.noDamageTo(dto)) {
            if (allTypeIds.contains(target.extractId())) {
                multipliers.put(target.extractId(), MULTIPLIER_IMMUNE);
            }
        }

        for (PokeApiResource target : typeIngestionHelper.halfDamageTo(dto)) {
            if (allTypeIds.contains(target.extractId())) {
                multipliers.put(target.extractId(), MULTIPLIER_HALF);
            }
        }

        for (PokeApiResource target : typeIngestionHelper.doubleDamageTo(dto)) {
            if (allTypeIds.contains(target.extractId())) {
                multipliers.put(target.extractId(), MULTIPLIER_DOUBLE);
            }
        }

        return multipliers;
    }
}