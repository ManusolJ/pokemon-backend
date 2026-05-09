package com.poketeambuilder.services.query;

import com.poketeambuilder.entities.Team;
import com.poketeambuilder.entities.TeamPokemon;

import com.poketeambuilder.infrastructure.exceptions.ResourceNotFoundException;

import com.poketeambuilder.dtos.front.move.MoveSummaryDto;
import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;
import com.poketeambuilder.dtos.front.team.team.TeamReadDto;
import com.poketeambuilder.dtos.front.team.team.TeamFilterDto;
import com.poketeambuilder.dtos.front.team.team.TeamSummaryDto;
import com.poketeambuilder.dtos.front.team.pokemon.TeamPokemonReadDto;
import com.poketeambuilder.dtos.front.team.pokemon.TeamPokemonMoveEmbedDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.TeamMapper;
import com.poketeambuilder.mappers.implementation.TeamPokemonMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.TeamRepository;
import com.poketeambuilder.repositories.TeamPokemonRepository;
import com.poketeambuilder.repositories.TeamPokemonMoveRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import org.springframework.stereotype.Service;
import org.springframework.cache.CacheManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.validation.annotation.Validated;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaQuery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: Implement slug-based retrieval
@Service
@Validated
public class TeamQueryService extends AbstractQueryService<Team, Long, TeamFilterDto, TeamReadDto> {

    private final TeamMapper teamMapper;
    private final TeamRepository teamRepository;
    private final TeamPokemonMapper teamPokemonMapper;
    private final TeamPokemonRepository teamPokemonRepository;
    private final TeamPokemonMoveRepository teamPokemonMoveRepository;

    public TeamQueryService(CacheManager cacheManager, TeamMapper teamMapper, TeamRepository teamRepository,
                            TeamPokemonMapper teamPokemonMapper, TeamPokemonRepository teamPokemonRepository,
                            TeamPokemonMoveRepository teamPokemonMoveRepository) {
        super(cacheManager);
        this.teamMapper = teamMapper;
        this.teamRepository = teamRepository;
        this.teamPokemonMapper = teamPokemonMapper;
        this.teamPokemonRepository = teamPokemonRepository;
        this.teamPokemonMoveRepository = teamPokemonMoveRepository;
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
        return null;
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

    @Override
    public TeamReadDto findById(@NotNull Long id) {
        TeamReadDto dto = super.findById(id);
        Map<Long, List<TeamPokemonReadDto>> pokemonMap = fetchPokemonForTeams(List.of(id));
        return withPokemon(dto, pokemonMap.getOrDefault(id, List.of()));
    }

    @Override
    public Page<TeamReadDto> filterEntities(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable) {
        Page<TeamReadDto> page = super.filterEntities(filter, pageable);

        List<Long> ids = page.getContent().stream().map(TeamReadDto::id).toList();
        if (ids.isEmpty()) return page;

        Map<Long, List<TeamPokemonReadDto>> pokemonMap = fetchPokemonForTeams(ids);

        List<TeamReadDto> enriched = page.getContent().stream()
                .map(dto -> withPokemon(dto, pokemonMap.getOrDefault(dto.id(), List.of())))
                .toList();

        return new PageImpl<>(enriched, pageable, page.getTotalElements());
    }

    public TeamReadDto findPublicTeamById(@NotNull Long id) {
        TeamReadDto publicTeam = this.findById(id);

        if (!publicTeam.isPublic()) {
            throw new ResourceNotFoundException(String.format("Team with id %s is private.", publicTeam.id()));
        }

        return publicTeam;
    }

    public Page<TeamSummaryDto> filterPublicSummaries(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable) {
        filter.setIsPublic(true);
        return filterAndMap(filter, pageable, teamMapper::toSummaryDto);
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

    private Map<Long, List<TeamPokemonReadDto>> fetchPokemonForTeams(List<Long> teamIds) {
        List<TeamPokemon> teamPokemons = teamPokemonRepository.findByTeamIdInWithDetails(teamIds);

        List<Long> teamPokemonIds = teamPokemons.stream().map(TeamPokemon::getId).toList();
        Map<Long, List<TeamPokemonMoveEmbedDto>> movesMap = fetchMovesForTeamPokemon(teamPokemonIds);

        return teamPokemons.stream().collect(Collectors.groupingBy(
                tp -> tp.getTeam().getId(),
                Collectors.mapping(
                        tp -> withMoves(teamPokemonMapper.toReadDto(tp), movesMap.getOrDefault(tp.getId(), List.of())),
                        Collectors.toList()
                )
        ));
    }

    private Map<Long, List<TeamPokemonMoveEmbedDto>> fetchMovesForTeamPokemon(List<Long> teamPokemonIds) {
        if (teamPokemonIds.isEmpty()) return Map.of();

        return teamPokemonMoveRepository.findByTeamPokemonIdInWithMove(teamPokemonIds).stream()
                .collect(Collectors.groupingBy(
                        tpm -> tpm.getId().getTeamPokemonId(),
                        Collectors.mapping(
                                tpm -> new TeamPokemonMoveEmbedDto(
                                        new MoveSummaryDto(
                                                tpm.getMove().getId(),
                                                tpm.getMove().getName(),
                                                new TypeReadDto(tpm.getMove().getType().getId(), tpm.getMove().getType().getName())
                                        ),
                                        tpm.getId().getSlotPosition()
                                ),
                                Collectors.toList()
                        )
                ));
    }

    private TeamReadDto withPokemon(TeamReadDto dto, List<TeamPokemonReadDto> pokemon) {
        return new TeamReadDto(
                dto.id(), dto.name(), dto.slug(), dto.isPublic(), dto.likeCount(),
                dto.createdAt(), dto.updatedAt(), dto.owner(), pokemon
        );
    }

    private TeamPokemonReadDto withMoves(TeamPokemonReadDto dto, List<TeamPokemonMoveEmbedDto> moves) {
        return new TeamPokemonReadDto(
                dto.id(), dto.slot(), dto.nickname(), dto.level(), dto.gender(), dto.shiny(),
                dto.pokemon(), dto.ability(), dto.nature(), dto.heldItem(), dto.teraType(),
                dto.evHp(), dto.evAtk(), dto.evDef(), dto.evSpAtk(), dto.evSpDef(), dto.evSpeed(),
                dto.ivHp(), dto.ivAtk(), dto.ivDef(), dto.ivSpAtk(), dto.ivSpDef(), dto.ivSpeed(),
                moves
        );
    }
}