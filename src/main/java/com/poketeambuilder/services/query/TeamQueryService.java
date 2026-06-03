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
 * Read access to {@link Team}. Each entry point resolves the current user's id once (when
 * a username is supplied) and threads it through the helpers so the like-membership query
 * shares a single user lookup with the rest of the call.
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
        query.distinct(true);
    }

    @Override
    public TeamReadDto findById(@NotNull Long id) {
        return findById(id, null);
    }

    /** {@link #findById(Long)} with the requester's username (for the {@code likedByCurrentUser} flag). */
    public TeamReadDto findById(@NotNull Long id, String currentUsername) {
        TeamReadDto dto = super.findById(id);
        Long userId = resolveUserId(currentUsername);
        Map<Long, List<TeamPokemonReadDto>> pokemonMap = fetchPokemonForTeams(List.of(id));
        Set<Long> likedIds = fetchLikedTeamIds(userId, List.of(id));
        return withPokemonAndLike(dto, pokemonMap.getOrDefault(id, List.of()), likedIds.contains(id));
    }

    @Override
    public Page<TeamReadDto> filterEntities(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable) {
        return filterEntities(filter, pageable, null);
    }

    /** {@link #filterEntities(TeamFilterDto, Pageable)} with the requester's username. */
    public Page<TeamReadDto> filterEntities(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable, String currentUsername) {
        Page<TeamReadDto> page = super.filterEntities(filter, pageable);

        List<Long> ids = page.getContent().stream().map(TeamReadDto::id).toList();
        if (ids.isEmpty()) {
            return page;
        }

        Long userId = resolveUserId(currentUsername);
        Map<Long, List<TeamPokemonReadDto>> pokemonMap = fetchPokemonForTeams(ids);
        Set<Long> likedIds = fetchLikedTeamIds(userId, ids);

        List<TeamReadDto> enriched = page.getContent().stream()
                .map(dto -> withPokemonAndLike(dto, pokemonMap.getOrDefault(dto.id(), List.of()), likedIds.contains(dto.id())))
                .toList();

        return new PageImpl<>(enriched, pageable, page.getTotalElements());
    }

    /** Public read of a team by id. Refuses to serve private teams (404 instead of 403 to avoid leaking existence). */
    public TeamReadDto findPublicTeamById(@NotNull Long id, String currentUsername) {
        TeamReadDto publicTeam = this.findById(id, currentUsername);

        if (!publicTeam.isPublic()) {
            throw new ResourceNotFoundException(String.format("Team with id %s is private.", publicTeam.id()));
        }

        return publicTeam;
    }

    /** Owner-only read by id. Refuses to serve teams owned by other users. */
    public TeamReadDto findOwnedTeamById(@NotNull Long id, @NotNull String currentUsername) {
        TeamReadDto team = this.findById(id, currentUsername);

        if (team.owner() == null || !currentUsername.equals(team.owner().username())) {
            throw new ResourceNotFoundException(String.format("Team with id %s not found for user '%s'", id, currentUsername));
        }

        return team;
    }

    /** Returns the team only if it's public OR owned by the requester; 404 otherwise. */
    public TeamReadDto findVisibleTeamById(@NotNull Long id, String currentUsername) {
        TeamReadDto team = this.findById(id, currentUsername);

        if (!team.isPublic()) {
            if (currentUsername == null || team.owner() == null || !currentUsername.equals(team.owner().username())) {
                throw new ResourceNotFoundException(String.format("Team with id %s not found", id));
            }
        }

        return team;
    }

    /** Teams summary listing. Layered as a defensive copy + userId override on the filter. */
    public Page<TeamSummaryDto> filterOwnedSummaries(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable, @NotNull String currentUsername) {
        Long userId = userRepository.findByUsernameAndDeletedAtIsNull(currentUsername)
                .map(AppUser::getId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User '%s' not found", currentUsername)));

        TeamFilterDto scoped = filter.copy();
        scoped.setUserId(userId);
        return filterSummaries(scoped, pageable, currentUsername);
    }

    /** Public team summary listing. Same as {@link #filterSummaries} but with {@code isPublic = true} forced. */
    public Page<TeamSummaryDto> filterPublicSummaries(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable, String currentUsername) {
        TeamFilterDto scoped = filter.copy();
        scoped.setIsPublic(true);
        return filterSummaries(scoped, pageable, currentUsername);
    }

    /** Generic team summary listing. Enriches each row with sprite URLs + the liked-by-me flag. */
    public Page<TeamSummaryDto> filterSummaries(@Valid @NotNull TeamFilterDto filter, @NotNull Pageable pageable, String currentUsername) {
        Page<TeamSummaryDto> page = filterAndMap(filter, pageable, teamMapper::toSummaryDto);

        List<Long> ids = page.getContent().stream().map(TeamSummaryDto::id).toList();
        if (ids.isEmpty()) {
            return page;
        }

        Long userId = resolveUserId(currentUsername);
        Map<Long, List<String>> spritesMap = fetchSpritesForTeams(ids);
        Set<Long> likedIds = fetchLikedTeamIds(userId, ids);

        List<TeamSummaryDto> enriched = page.getContent().stream()
                .map(dto -> withSpritesAndLike(dto, spritesMap.getOrDefault(dto.id(), List.of()), likedIds.contains(dto.id())))
                .toList();

        return new PageImpl<>(enriched, pageable, page.getTotalElements());
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

    /** Resolves the username to a user id once per service call, or returns {@code null} for anonymous callers. */
    private Long resolveUserId(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return userRepository.findByUsernameAndDeletedAtIsNull(username).map(AppUser::getId).orElse(null);
    }

    private Map<Long, List<TeamPokemonReadDto>> fetchPokemonForTeams(List<Long> teamIds) {
        List<TeamPokemon> teamPokemons = teamPokemonRepository.findByTeamIdInWithDetails(teamIds);

        List<Long> teamPokemonIds = teamPokemons.stream().map(TeamPokemon::getId).toList();
        Map<Long, List<TeamPokemonMoveEmbedDto>> movesMap = fetchMovesForTeamPokemon(teamPokemonIds);

        return teamPokemons.stream().collect(Collectors.groupingBy(
                tp -> tp.getTeam().getId(),
                Collectors.mapping(
                        tp -> withMoves(teamPokemonMapper.toReadDto(tp), movesMap.getOrDefault(tp.getId(), List.of())),
                        Collectors.toList())));
    }

    /** Pulls the per-slot sprite URLs only. UIsed by the summary listings to avoid the full join. */
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

    private TeamReadDto withPokemonAndLike(TeamReadDto dto, List<TeamPokemonReadDto> pokemon, boolean liked) {
        return new TeamReadDto(
                dto.id(), dto.name(), dto.slug(), dto.isPublic(), dto.likeCount(),
                dto.createdAt(), dto.updatedAt(), dto.owner(), pokemon, liked);
    }

    private TeamSummaryDto withSpritesAndLike(TeamSummaryDto dto, List<String> sprites, boolean liked) {
        return new TeamSummaryDto(
                dto.id(), dto.name(), dto.slug(), dto.isPublic(), dto.likeCount(), dto.createdAt(),dto.updatedAt() ,dto.owner(), sprites, liked);
    }

    private TeamPokemonReadDto withMoves(TeamPokemonReadDto dto, List<TeamPokemonMoveEmbedDto> moves) {
        return new TeamPokemonReadDto(
                dto.id(), dto.slot(), dto.nickname(), dto.level(), dto.gender(), dto.shiny(),
                dto.pokemon(), dto.ability(), dto.nature(), dto.heldItem(), dto.teraType(),
                dto.evHp(), dto.evAtk(), dto.evDef(), dto.evSpAtk(), dto.evSpDef(), dto.evSpeed(),
                dto.ivHp(), dto.ivAtk(), dto.ivDef(), dto.ivSpAtk(), dto.ivSpDef(), dto.ivSpeed(),
                moves);
    }
}
