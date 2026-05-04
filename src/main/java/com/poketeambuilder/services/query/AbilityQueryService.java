package com.poketeambuilder.services.query;

import com.poketeambuilder.entities.Ability;

import com.poketeambuilder.dtos.front.ability.AbilityReadDto;
import com.poketeambuilder.dtos.front.ability.AbilityFilterDto;
import com.poketeambuilder.dtos.front.ability.AbilitySummaryDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.AbilityMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.AbilityRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import org.springframework.stereotype.Service;

import org.springframework.cache.CacheManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class AbilityQueryService extends AbstractQueryService<Ability, Integer, AbilityFilterDto, AbilityReadDto> {

    private AbilityMapper abilityMapper;
    private AbilityRepository abilityRepository;

    public AbilityQueryService(CacheManager cacheManager, AbilityMapper abilityMapper, AbilityRepository abilityRepository) {
        super(cacheManager);
        this.abilityMapper = abilityMapper;
        this.abilityRepository = abilityRepository;
    }

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";

    @Override
    protected String getEntityName() {
        return "Ability";
    }

    @Override
    protected String getCacheName() {
        return "abilities";
    }

    @Override
    protected ReadMapper<Ability, AbilityReadDto> getMapper() {
        return abilityMapper;
    }

    @Override
    protected BaseRepository<Ability, Integer> getRepository() {
        return abilityRepository;
    }

    public Page<AbilitySummaryDto> filterAbilitySummaries(@Valid @NotNull AbilityFilterDto filter, @NotNull Pageable pageable) {
        return filterAndMap(filter, pageable, abilityMapper::toSummaryDto);
    }

    @Override
    protected Specification<Ability> buildSpecification(AbilityFilterDto filter) {
        SpecificationBuilder<Ability> builder = new SpecificationBuilder<>();

        if (!filter.hasAnyCriteria()) {
            return builder.build();
        }

        if (filter.getId() != null) {
            builder.with(FIELD_ID, filter.getId(), SearchOperation.EQUAL);
        }

        if (filter.getName() != null && !filter.getName().isEmpty()) {
            builder.with(FIELD_NAME, filter.getName(), SearchOperation.LIKE);
        }

        if (filter.getNameExact() != null && !filter.getNameExact().isEmpty()) {
            builder.with(FIELD_NAME, filter.getNameExact(), SearchOperation.EQUAL);
        }

        return builder.build();
    }
    
}
