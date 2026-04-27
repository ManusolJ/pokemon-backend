package com.poketeambuilder.services.query;

import org.springframework.stereotype.Service;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.validation.annotation.Validated;

import com.poketeambuilder.entities.Type;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;
import com.poketeambuilder.dtos.front.type.typing.TypeFilterDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.TypeMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.TypeRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class TypeQueryService extends AbstractQueryService<Type, Integer, TypeFilterDto, TypeReadDto> {

    private final TypeMapper typeMapper;
    private final TypeRepository typeRepository;

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";

    @Override
    protected String getEntityName() {
        return "Type";
    }

    @Override
    protected ReadMapper<Type, TypeReadDto> getMapper() {
        return typeMapper;
    }

    @Override
    protected BaseRepository<Type, Integer> getRepository() {
        return typeRepository;
    }

    @Override
    protected Specification<Type> buildSpecification(@NotNull TypeFilterDto filter) {
        SpecificationBuilder<Type> builder = new SpecificationBuilder<>();

        if (!filter.hasAnyCriteria()) {
            return builder.build();
        }

        if (filter.getId() != null) {
            builder.with(FIELD_ID, filter.getId(), SearchOperation.EQUAL);
        }
        if (filter.getName() != null && !filter.getName().isBlank()) {
            builder.with(FIELD_NAME, filter.getName(), SearchOperation.LIKE);
        }
        if (filter.getNameExact() != null && !filter.getNameExact().isBlank()) {
            builder.with(FIELD_NAME, filter.getNameExact(), SearchOperation.EQUAL);
        }

        return builder.build();
    }
}