package com.poketeambuilder.services.query;

import com.poketeambuilder.entities.TypeEffectiveness;
import com.poketeambuilder.entities.compositeIDs.TypeEffectivenessId;

import com.poketeambuilder.dtos.front.type.effectiveness.TypeEffectivenessFilterDto;
import com.poketeambuilder.dtos.front.type.effectiveness.TypeEffectivenessReadDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.TypeEffectivenessMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.TypeEffectivenessRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import org.springframework.stereotype.Service;

import org.springframework.cache.CacheManager;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.validation.annotation.Validated;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaQuery;

import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class TypeEffectivenessQueryService extends AbstractQueryService<TypeEffectiveness, TypeEffectivenessId, TypeEffectivenessFilterDto, TypeEffectivenessReadDto> {

    private final TypeEffectivenessMapper typeEffectivenessMapper;
    private final TypeEffectivenessRepository typeEffectivenessRepository;

    public TypeEffectivenessQueryService(CacheManager cacheManager, TypeEffectivenessMapper typeEffectivenessMapper, TypeEffectivenessRepository typeEffectivenessRepository) {
        super(cacheManager);
        this.typeEffectivenessMapper = typeEffectivenessMapper;
        this.typeEffectivenessRepository = typeEffectivenessRepository;
    }

    private static final String FIELD_MULTIPLIER = "multiplier";
    private static final String FIELD_ATTACKING_TYPE_ID = "attackingType.id";
    private static final String FIELD_DEFENDING_TYPE_ID = "defendingType.id";

    @Override
    protected String getEntityName() {
        return "TypeEffectiveness";
    }

    @Override
    protected String getCacheName() {
        return "typeEffectiveness";
    }

    @Override
    protected ReadMapper<TypeEffectiveness, TypeEffectivenessReadDto> getMapper() {
        return typeEffectivenessMapper;
    }

    @Override
    protected BaseRepository<TypeEffectiveness, TypeEffectivenessId> getRepository() {
        return typeEffectivenessRepository;
    }

    @Override
    protected void applyFetches(Root<TypeEffectiveness> root, CriteriaQuery<?> query) {
        root.fetch("attackingType");
        root.fetch("defendingType");
    }

    @Override
    protected Specification<TypeEffectiveness> buildSpecification(@NotNull TypeEffectivenessFilterDto filter) {
        SpecificationBuilder<TypeEffectiveness> builder = new SpecificationBuilder<>();

        if (!filter.hasAnyCriteria()) {
            return builder.build();
        }

        if (filter.getAttackingTypeId() != null) {
            builder.with(FIELD_ATTACKING_TYPE_ID, filter.getAttackingTypeId(), SearchOperation.EQUAL);
        }

        if (filter.getDefendingTypeId() != null) {
            builder.with(FIELD_DEFENDING_TYPE_ID, filter.getDefendingTypeId(), SearchOperation.EQUAL);
        }

        if (filter.getMultiplier() != null) {
            builder.with(FIELD_MULTIPLIER, filter.getMultiplier(), SearchOperation.EQUAL);
        }

        return builder.build();
    }
}