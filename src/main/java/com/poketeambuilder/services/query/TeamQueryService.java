package com.poketeambuilder.services.query;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.poketeambuilder.entities.Team;
import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.TeamPokemon;

import com.poketeambuilder.infrastructure.exceptions.ResourceNotFoundException;

import com.poketeambuilder.dtos.front.move.MoveSummaryDto;
import com.poketeambuilder.dtos.front.type.single.TypeReadDto;
import com.poketeambuilder.dtos.front.team.details.TeamReadDto;
import com.poketeambuilder.dtos.front.team.details.TeamFilterDto;
import com.poketeambuilder.dtos.front.team.details.TeamSummaryDto;
import com.poketeambuilder.dtos.front.team.roster.TeamPokemonReadDto;
import com.poketeambuilder.dtos.front.team.roster.TeamPokemonMoveEmbedDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.TeamMapper;
import com.poketeambuilder.mappers.implementation.TeamPokemonMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.UserRepository;
import com.poketeambuilder.repositories.TeamRepository;
import com.poketeambuilder.repositories.TeamLikeRepository;
import com.poketeambuilder.repositories.TeamPokemonRepository;
import com.poketeambuilder.repositories.TeamPokemonMoveRepository;
import com.poketeambuilder.repositories.projections.TeamSpriteProjection;

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

/**
 * Read access to {@link Team}. Entry points take the requester's user id (or {@code null} for
 * anonymous callers) and thread it through the helpers for the like-membership query.
 *
 * <p>Listings load roster details (full join graph) or sprite-only projections depending on
 * whether the caller needs read DTOs or summaries.</p>
 */
@Service
@Validated
public class TeamQueryService extends AbstractQueryService<Team, Long, TeamFilterDto, TeamReadDto> {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SLUG = "slug";
    private static final String FIELD_IS_PUBLIC = "isPublic";
    private static final String FIELD_OWNER_ID = "owner.id";
    private static final String FIELD_OWNER_DELETED_AT = "owner.deletedAt";

    private final TeamMapper teamMapper;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamPokemonMapper teamPokemonMapper;
    private final TeamLikeRepository teamLikeRepository;
    private final TeamPokemonRepository teamPokemonRepository;
    private final TeamPokemonMoveRepository teamPokemonMoveRepository;

    public TeamQueryService(CacheManager cacheManager, TeamMapper teamMapper, TeamRepository teamRepository,
                            TeamPokemonMapper teamPokemonMapper, TeamPokemonRepository teamPokemonRepository,
                            TeamPokemonMoveRepository teamPokemonMoveRepository,
                            TeamLikeRepository teamLikeRepository, UserRepository userRepository) {
        super(cacheManager);
        this.teamMapper = teamMapper;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.teamPokemonMapper = teamPokemonMapper;
        this.teamLikeRepository = teamLikeRepository;
        this.teamPokemonRepository = teamPokemonRepository;
        this.teamPokemonMoveRepository = teamPokemonMoveRepository;
    }

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
    }

    @Override
    public TeamReadDto findById(@NotNull Long id) {
        return findById(id, null);
    }

    /** {@link #findById(Long)} with the requester's user id (for the {@code likedByCurrentUser} flag). */
    public TeamReadDto findById(@NotNull Long id, Long currentUserId) {
        TeamReadDto dto = super.findById(id);
        Map<Long, List<TeamPokemonReadDto>> pokemonMap = fetchPokemonForTeams(List.of(id));
        Set<Long> likedIds = fetchLikedTeamIds(currentUserId, List.of(id));
        return dto.withRosterAndLike(pokemonMap.getOrDefault(id, List.of()), likedIds.contains(id));
    }

    /**
     * Full-DTO listing. No endpoint routes here today - the controller serves summaries - but the
     * override has to exist: {@code AbstractQueryService.filterEntities} maps through
     * {@code TeamMapper.toReadDto}, which ignores {@code pokemon} and {@code likedByCurrentUser},
     * so inheriting it would hand back rosterless teams with the like flag always false. The
     * enrichment below is what makes the inherited contract truthful.
     */
    @Override
    public Page<TeamReadDto> filterEntities(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable) {
        return filterEntities(filter, pageable, null);
    }

    /** {@link #filterEntities(TeamFilterDto, Pageable)} with the requester's user id. */
    public Page<TeamReadDto> filterEntities(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable, Long currentUserId) {
        Page<TeamReadDto> page = super.filterEntities(filter, pageable);

        List<Long> ids = page.getContent().stream().map(TeamReadDto::id).toList();
        if (ids.isEmpty()) {
            return page;
        }

        Map<Long, List<TeamPokemonReadDto>> pokemonMap = fetchPokemonForTeams(ids);
        Set<Long> likedIds = fetchLikedTeamIds(currentUserId, ids);

        List<TeamReadDto> enriched = page.getContent().stream()
                .map(dto -> dto.withRosterAndLike(pokemonMap.getOrDefault(dto.id(), List.of()), likedIds.contains(dto.id())))
                .toList();

        return new PageImpl<>(enriched, pageable, page.getTotalElements());
    }

    /**
     * Public read of a team by id. Refuses to serve private teams, and teams belonging to a
     * tombstoned account, with a 404 rather than a 403 so neither case leaks existence.
     */
    public TeamReadDto findPublicTeamById(@NotNull Long id, Long currentUserId) {
        TeamReadDto publicTeam = this.findById(id, currentUserId);

        if (!publicTeam.isPublic() || !teamRepository.existsByIdAndOwnerDeletedAtIsNull(id)) {
            throw new ResourceNotFoundException(String.format("Team with id %s not found", id));
        }

        return publicTeam;
    }

    /**
     * Read-only view through a share link. Serves private teams too - holding the slug is the
     * permission - but not teams of a tombstoned account. An unknown slug and a tombstoned owner
     * answer the same 404.
     */
    public TeamReadDto findSharedTeam(@NotNull String slug, Long currentUserId) {
        Long teamId = teamRepository.findShareableIdBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Shared team not found"));

        return this.findById(teamId, currentUserId);
    }

    /** Owner-only read by id. Refuses to serve teams owned by other users. */
    public TeamReadDto findOwnedTeamById(@NotNull Long id, @NotNull Long currentUserId) {
        TeamReadDto team = this.findById(id, currentUserId);

        if (team.owner() == null || team.owner().id() != currentUserId) {
            throw new ResourceNotFoundException(String.format("Team with id %s not found", id));
        }

        return team;
    }

    /** Teams summary listing. Layered as a defensive copy + userId override on the filter. */
    public Page<TeamSummaryDto> filterOwnedSummaries(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable, @NotNull Long currentUserId) {
        Long userId = userRepository.findByIdAndDeletedAtIsNull(currentUserId)
                .map(AppUser::getId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with id '%s' not found", currentUserId)));

        TeamFilterDto scoped = filter.copy();
        scoped.setUserId(userId);
        return filterSummaries(scoped, pageable, userId);
    }

    /**
     * Public team summary listing. Same as {@link #filterSummaries} but with
     * {@code isPublic = true} and an active owner forced, so a client can't widen the scope
     * and a tombstoned account's teams drop out of the listing.
     */
    public Page<TeamSummaryDto> filterPublicSummaries(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable, Long currentUserId) {
        TeamFilterDto scoped = filter.copy();
        scoped.setIsPublic(true);
        scoped.setOwnerActive(true);
        return filterSummaries(scoped, pageable, currentUserId);
    }

    /** Generic team summary listing. Enriches each row with sprite URLs + the liked-by-me flag. */
    private Page<TeamSummaryDto> filterSummaries(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable, Long currentUserId) {
        Page<TeamSummaryDto> page = filterAndMap(filter, pageable, teamMapper::toSummaryDto);

        List<Long> ids = page.getContent().stream().map(TeamSummaryDto::id).toList();
        if (ids.isEmpty()) {
            return page;
        }

        Map<Long, List<String>> spritesMap = fetchSpritesForTeams(ids);
        Set<Long> likedIds = fetchLikedTeamIds(currentUserId, ids);

        List<TeamSummaryDto> enriched = page.getContent().stream()
                .map(dto -> dto.withSpritesAndLike(spritesMap.getOrDefault(dto.id(), List.of()), likedIds.contains(dto.id())))
                .toList();

        return new PageImpl<>(enriched, pageable, page.getTotalElements());
    }

    @Override
    protected Specification<Team> buildSpecification(@NotNull TeamFilterDto filter) {
        SpecificationBuilder<Team> builder = new SpecificationBuilder<>();

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
        if (Boolean.TRUE.equals(filter.getOwnerActive())) {
            builder.with(FIELD_OWNER_DELETED_AT, null, SearchOperation.IS_NULL);
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
                        tp -> teamPokemonMapper.toReadDto(tp).withMoves(movesMap.getOrDefault(tp.getId(), List.of())),
                        Collectors.toList())));
    }

    /** Pulls the per-slot sprite URLs only. Used by the summary listings to avoid the full join. */
    private Map<Long, List<String>> fetchSpritesForTeams(List<Long> teamIds) {
        return teamPokemonRepository.findSpritesByTeamIdIn(teamIds).stream()
                .sorted(Comparator.comparing(TeamSpriteProjection::getSlot))
                .collect(Collectors.groupingBy(
                        TeamSpriteProjection::getTeamId,
                        Collectors.mapping(TeamSpriteProjection::getSpriteDefault, Collectors.toList())));
    }

    private Set<Long> fetchLikedTeamIds(Long userId, List<Long> teamIds) {
        if (userId == null || teamIds.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(teamLikeRepository.findLikedTeamIds(userId, teamIds));
    }

    private Map<Long, List<TeamPokemonMoveEmbedDto>> fetchMovesForTeamPokemon(List<Long> teamPokemonIds) {
        if (teamPokemonIds.isEmpty()) {
            return Map.of();
        }

        return teamPokemonMoveRepository.findByTeamPokemonIdInWithMove(teamPokemonIds).stream()
                .collect(Collectors.groupingBy(
                        tpm -> tpm.getId().getTeamPokemonId(),
                        Collectors.mapping(
                                tpm -> new TeamPokemonMoveEmbedDto(
                                        new MoveSummaryDto(
                                                tpm.getMove().getId(),
                                                tpm.getMove().getName(),
                                                new TypeReadDto(tpm.getMove().getType().getId(), tpm.getMove().getType().getName())),
                                        tpm.getId().getSlotPosition()),
                                Collectors.toList())));
    }
}
