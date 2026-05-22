package com.poketeambuilder.services.seed;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.math.BigDecimal;
import java.util.stream.Collectors;

import com.poketeambuilder.entities.Type;
import com.poketeambuilder.entities.TypeEffectiveness;

import com.poketeambuilder.dtos.front.admin.seed.SeedResultDto;

import com.poketeambuilder.dtos.pokeapi.type.TypeApiDto;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.poketeambuilder.mappers.implementation.TypeMapper;
import com.poketeambuilder.mappers.helpers.resource.TypeIngestionHelper;

import com.poketeambuilder.repositories.TypeRepository;
import com.poketeambuilder.repositories.TypeEffectivenessRepository;

import com.poketeambuilder.services.command.PokeApiClient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Seeds the {@code type} and {@code type_effectiveness} tables from PokeAPI. The 18 canonical
 * types are persisted first, then the 18 × 18 effectiveness matrix is constructed from each
 * type's damage relations.
 */
@Slf4j
@Service
public class TypeSeedService {

    private static final String TYPE_ENDPOINT = "/type";
    private static final int NON_CANON_TYPE_ID_THRESHOLD = 18;

    private static final BigDecimal MULTIPLIER_IMMUNE = BigDecimal.ZERO;
    private static final BigDecimal MULTIPLIER_HALF = new BigDecimal("0.50");
    private static final BigDecimal MULTIPLIER_DOUBLE = new BigDecimal("2.00");
    private static final BigDecimal MULTIPLIER_NEUTRAL = new BigDecimal("1.00");

    private final TypeMapper typeMapper;
    private final TypeRepository typeRepository;
    private final TypeIngestionHelper typeIngestionHelper;
    private final TypeEffectivenessRepository typeEffectivenessRepository;

    private final PokeApiClient pokeApiClient;
    private final TransactionTemplate transactionTemplate;

    public TypeSeedService(TypeMapper typeMapper, TypeRepository typeRepository,
                           TypeIngestionHelper typeIngestionHelper,
                           TypeEffectivenessRepository typeEffectivenessRepository,
                           PokeApiClient pokeApiClient, TransactionTemplate transactionTemplate) {
        this.typeMapper = typeMapper;
        this.pokeApiClient = pokeApiClient;
        this.typeRepository = typeRepository;
        this.transactionTemplate = transactionTemplate;
        this.typeIngestionHelper = typeIngestionHelper;
        this.typeEffectivenessRepository = typeEffectivenessRepository;
    }

    public SeedResultDto seed() {
        FetchResult fetched = fetchAll();
        PersistResult persisted = transactionTemplate.execute(status -> persist(fetched.apiDtos()));

        log.info("Seeded {} types and {} effectiveness entries ({} fetch errors)",
                persisted.typesSaved(), persisted.effectivenessSaved(), fetched.errors());

        return SeedResultDto.of(persisted.typesSaved() + persisted.effectivenessSaved(), fetched.errors());
    }

    @Transactional
    public void clearSeedData() {
        typeEffectivenessRepository.deleteAllInBatch();
        typeRepository.deleteAllInBatch();
    }

    private FetchResult fetchAll() {
        List<PokeApiResource> resources = pokeApiClient.fetchAllResources(TYPE_ENDPOINT).stream()
                .filter(resource -> {
                    Integer id = resource.extractId();
                    return id != null && id <= NON_CANON_TYPE_ID_THRESHOLD;
                })
                .toList();

        List<TypeApiDto> apiDtos = new ArrayList<>();
        int errors = 0;

        for (PokeApiResource resource : resources) {
            try {
                apiDtos.add(pokeApiClient.fetchResource(resource.url(), TypeApiDto.class));
            } catch (Exception e) {
                errors++;
                log.error("Failed to fetch type resource: {}", resource.url(), e);
            }
        }

        return new FetchResult(apiDtos, errors);
    }

    private PersistResult persist(List<TypeApiDto> apiDtos) {
        List<Type> entities = apiDtos.stream().map(typeMapper::toEntity).toList();
        typeRepository.saveAllAndFlush(entities);

        int effectivenessCount = seedTypeEffectiveness(apiDtos);

        return new PersistResult(entities.size(), effectivenessCount);
    }

    private int seedTypeEffectiveness(List<TypeApiDto> apiDtos) {
        Set<Integer> allTypeIds = apiDtos.stream()
                .map(TypeApiDto::id)
                .collect(Collectors.toSet());

        List<TypeEffectiveness> entries = new ArrayList<>();

        for (TypeApiDto dto : apiDtos) {
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

        applyMultiplier(typeIngestionHelper.noDamageTo(dto), allTypeIds, multipliers, MULTIPLIER_IMMUNE);
        applyMultiplier(typeIngestionHelper.halfDamageTo(dto), allTypeIds, multipliers, MULTIPLIER_HALF);
        applyMultiplier(typeIngestionHelper.doubleDamageTo(dto), allTypeIds, multipliers, MULTIPLIER_DOUBLE);

        return multipliers;
    }

    private static void applyMultiplier(List<PokeApiResource> targets, Set<Integer> allTypeIds, Map<Integer, BigDecimal> multipliers, BigDecimal value) {
        for (PokeApiResource target : targets) {
            Integer id = target.extractId();
            if (id != null && allTypeIds.contains(id)) {
                multipliers.put(id, value);
            }
        }
    }

    private record FetchResult(List<TypeApiDto> apiDtos, int errors) {}

    private record PersistResult(int typesSaved, int effectivenessSaved) {}
}
