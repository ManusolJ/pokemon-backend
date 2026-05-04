package com.poketeambuilder.services.query;

import com.poketeambuilder.entities.SeedLog;

import com.poketeambuilder.dtos.front.admin.seed.SeedLogReadDto;
import com.poketeambuilder.dtos.front.admin.seed.SeedLogFilterDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.SeedLogMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.SeedLogRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import org.springframework.stereotype.Service;

import org.springframework.cache.CacheManager;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class SeedLogQueryService extends AbstractQueryService<SeedLog, Long, SeedLogFilterDto, SeedLogReadDto> {

    private final SeedLogMapper seedLogMapper;
    private final SeedLogRepository seedLogRepository;

    public SeedLogQueryService(CacheManager cacheManager, SeedLogMapper seedLogMapper, SeedLogRepository seedLogRepository) {
        super(cacheManager);
        this.seedLogMapper = seedLogMapper;
        this.seedLogRepository = seedLogRepository;
    }

    private static final String FIELD_ID = "id";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_STARTED_AT = "startedAt";
    private static final String FIELD_TRIGGERED_BY = "triggeredBy";

    @Override
    protected String getEntityName() {
        return "SeedLog";
    }

    @Override
    protected String getCacheName() {
        return null;
    }

    @Override
    protected ReadMapper<SeedLog, SeedLogReadDto> getMapper() {
        return seedLogMapper;
    }

    @Override
    protected BaseRepository<SeedLog, Long> getRepository() {
        return seedLogRepository;
    }

    @Override
    protected Specification<SeedLog> buildSpecification(@NotNull SeedLogFilterDto filter) {
        SpecificationBuilder<SeedLog> builder = new SpecificationBuilder<>();

        if (!filter.hasAnyCriteria()) {
            return builder.build();
        }

        if (filter.getId() != null) {
            builder.with(FIELD_ID, filter.getId(), SearchOperation.EQUAL);
        }

        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            builder.with(FIELD_STATUS, filter.getStatus(), SearchOperation.EQUAL);
        }

        if (filter.getTriggeredBy() != null && !filter.getTriggeredBy().isBlank()) {
            builder.with(FIELD_TRIGGERED_BY, filter.getTriggeredBy(), SearchOperation.LIKE);
        }

        if (filter.getTriggeredByExact() != null && !filter.getTriggeredByExact().isBlank()) {
            builder.with(FIELD_TRIGGERED_BY, filter.getTriggeredByExact(), SearchOperation.EQUAL);
        }

        if (filter.getDateFrom() != null) {
            builder.with(FIELD_STARTED_AT, filter.getDateFrom(), SearchOperation.GREATER_THAN_OR_EQUAL);
        }

        if (filter.getDateTo() != null) {
            builder.with(FIELD_STARTED_AT, filter.getDateTo(), SearchOperation.LESS_THAN_OR_EQUAL);
        }

        return builder.build();
    }
}