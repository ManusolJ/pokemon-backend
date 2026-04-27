package com.poketeambuilder.services.query;

import org.springframework.stereotype.Service;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.validation.annotation.Validated;

import com.poketeambuilder.dtos.front.ability.AbilityFilterDto;
import com.poketeambuilder.dtos.front.ability.AbilityReadDto;

import com.poketeambuilder.entities.Ability;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.AbilityMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.AbilityRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class AbilityQueryService extends AbstractQueryService<Ability, Integer, AbilityFilterDto, AbilityReadDto> {

    private AbilityMapper abilityMapper;
    private AbilityRepository abilityRepository;

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";

    @Override
    protected String getEntityName() {
        return "Ability";
    }

    @Override
    protected ReadMapper<Ability, AbilityReadDto> getMapper() {
        return abilityMapper;
    }

    @Override
    protected BaseRepository<Ability, Integer> getRepository() {
        return abilityRepository;
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
