package com.poketeambuilder.services.query;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.validation.annotation.Validated;

import com.poketeambuilder.dtos.front.nature.NatureReadDto;
import com.poketeambuilder.dtos.front.nature.NatureFilterDto;

import com.poketeambuilder.entities.Nature;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.NatureMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.NatureRepository;
import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class NatureQueryService extends AbstractQueryService<Nature, Integer, NatureFilterDto, NatureReadDto> {

    private NatureMapper natureMapper;
    private NatureRepository natureRepository;

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";

    @Override
    protected String getEntityName() {
        return "Nature";
    }

    @Override
    protected ReadMapper<Nature, NatureReadDto> getMapper() {
        return natureMapper;
    }

    @Override
    protected BaseRepository<Nature, Integer> getRepository() {
        return natureRepository;
    }

    @Override
    protected Specification<Nature> buildSpecification(NatureFilterDto filter) {
        SpecificationBuilder<Nature> builder = new SpecificationBuilder<>();

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
