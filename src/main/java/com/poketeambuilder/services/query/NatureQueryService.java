package com.poketeambuilder.services.query;

import com.poketeambuilder.entities.Nature;

import com.poketeambuilder.dtos.front.nature.NatureReadDto;
import com.poketeambuilder.dtos.front.nature.NatureFilterDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.NatureMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.NatureRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import org.springframework.stereotype.Service;

import org.springframework.cache.CacheManager;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class NatureQueryService extends AbstractQueryService<Nature, Integer, NatureFilterDto, NatureReadDto> {

    private final NatureMapper natureMapper;
    private final NatureRepository natureRepository;

    public NatureQueryService(CacheManager cacheManager, NatureMapper natureMapper, NatureRepository natureRepository) {
        super(cacheManager);
        this.natureMapper = natureMapper;
        this.natureRepository = natureRepository;
    }

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";

    @Override
    protected String getEntityName() {
        return "Nature";
    }

    @Override
    protected String getCacheName() {
        return "natures";
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
    protected Specification<Nature> buildSpecification(@NotNull NatureFilterDto filter) {
        SpecificationBuilder<Nature> builder = new SpecificationBuilder<>();

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
