package com.poketeambuilder.services.query;

import com.poketeambuilder.entities.Team;

import com.poketeambuilder.dtos.front.team.team.TeamReadDto;
import com.poketeambuilder.dtos.front.team.team.TeamFilterDto;
import com.poketeambuilder.dtos.front.team.team.TeamSummaryDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.TeamMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.TeamRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import org.springframework.stereotype.Service;

import org.springframework.cache.CacheManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.validation.annotation.Validated;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaQuery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// TODO: Implement slug-based retrieval
@Service
@Validated
public class TeamQueryService extends AbstractQueryService<Team, Long, TeamFilterDto, TeamReadDto> {

    private final TeamMapper teamMapper;
    private final TeamRepository teamRepository;

    public TeamQueryService(CacheManager cacheManager, TeamMapper teamMapper, TeamRepository teamRepository) {
        super(cacheManager);
        this.teamMapper = teamMapper;
        this.teamRepository = teamRepository;
    }

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SLUG = "slug";
    private static final String FIELD_IS_PUBLIC = "isPublic";
    private static final String FIELD_OWNER_ID = "owner.id";

    @Override
    protected String getEntityName() {
        return "Team";
    }

    @Override
    protected String getCacheName() {
        return "teams";
    }

    @Override
    protected ReadMapper<Team, TeamReadDto> getMapper() {
        return teamMapper;
    }

    @Override
    protected BaseRepository<Team, Long> getRepository() {
        return teamRepository;
    }

    @Override
    protected void applyFetches(Root<Team> root, CriteriaQuery<?> query) {
        root.fetch("owner");
        query.distinct(true);
    }

    public Page<TeamSummaryDto> filterSummaries(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable) {
        return filterAndMap(filter, pageable, teamMapper::toSummaryDto);
    }

    @Override
    protected Specification<Team> buildSpecification(@NotNull TeamFilterDto filter) {
        SpecificationBuilder<Team> builder = new SpecificationBuilder<>();

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
        if (filter.getSlug() != null && !filter.getSlug().isBlank()) {
            builder.with(FIELD_SLUG, filter.getSlug(), SearchOperation.EQUAL);
        }
        if (filter.getIsPublic() != null) {
            builder.with(FIELD_IS_PUBLIC, filter.getIsPublic(), SearchOperation.EQUAL);
        }
        if (filter.getUserId() != null) {
            builder.with(FIELD_OWNER_ID, filter.getUserId(), SearchOperation.EQUAL);
        }

        return builder.build();
    }
}