package com.poketeambuilder.services.command;

import java.util.Set;
import java.util.List;
import java.util.UUID;

import com.poketeambuilder.infrastructure.exceptions.ResourceNotFoundException;
import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;
import com.poketeambuilder.infrastructure.exceptions.IllegalTeamCompositionException;

import com.poketeambuilder.dtos.front.team.details.TeamCreateDto;
import com.poketeambuilder.dtos.front.team.details.TeamPatchDto;
import com.poketeambuilder.dtos.front.team.details.TeamUpdateDto;
import com.poketeambuilder.dtos.front.team.roster.TeamPokemonCreateDto;

import com.poketeambuilder.entities.Team;
import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.TeamLike;
import com.poketeambuilder.entities.TeamPokemon;
import com.poketeambuilder.entities.TeamPokemonMove;

import com.poketeambuilder.entities.compositeIDs.TeamLikeId;
import com.poketeambuilder.entities.compositeIDs.TeamPokemonMoveId;

import com.poketeambuilder.mappers.implementation.TeamMapper;
import com.poketeambuilder.mappers.implementation.TeamPokemonMapper;

import com.poketeambuilder.repositories.ItemRepository;
import com.poketeambuilder.repositories.MoveRepository;
import com.poketeambuilder.repositories.TeamRepository;
import com.poketeambuilder.repositories.TypeRepository;
import com.poketeambuilder.repositories.UserRepository;
import com.poketeambuilder.repositories.NatureRepository;
import com.poketeambuilder.repositories.AbilityRepository;
import com.poketeambuilder.repositories.PokemonRepository;
import com.poketeambuilder.repositories.TeamLikeRepository;
import com.poketeambuilder.repositories.PokemonMoveRepository;
import com.poketeambuilder.repositories.TeamPokemonRepository;
import com.poketeambuilder.repositories.PokemonAbilityRepository;
import com.poketeambuilder.repositories.TeamPokemonMoveRepository;

import com.poketeambuilder.utils.enums.UserRole;
import com.poketeambuilder.utils.enums.AuditAction;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

/**
 * Mutating operations for {@link Team} ownership, roster, and like state
 *
 * <p>The {@code adminDeleteTeam} path keeps an in-service role check as defense-in-depth on
 * top of {@code @PreAuthorize("hasRole('ADMIN')")} on the controller.</p>
 */
@Service
@Validated
@RequiredArgsConstructor
public class TeamCommandService {

    private static final String ENTITY_NAME = "Team";

    private final TeamMapper teamMapper;
    private final TeamPokemonMapper teamPokemonMapper;

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamLikeRepository teamLikeRepository;
    private final TeamPokemonRepository teamPokemonRepository;
    private final TeamPokemonMoveRepository teamPokemonMoveRepository;

    private final ItemRepository itemRepository;
    private final TypeRepository typeRepository;
    private final MoveRepository moveRepository;
    private final NatureRepository natureRepository;
    private final AbilityRepository abilityRepository;
    private final PokemonRepository pokemonRepository;
    private final PokemonMoveRepository pokemonMoveRepository;
    private final PokemonAbilityRepository pokemonAbilityRepository;

    private final AuditLogCommandService auditLogCommandService;

    /** Creates a team owned by the authenticated user with a fresh share slug. */
    @Transactional
    public Long createTeam(@NotNull Long userId, @Valid @NotNull TeamCreateDto dto) {
        AppUser owner = findUserOrThrow(userId);

        Team team = teamMapper.toEntity(dto);
        team.setOwner(owner);
        team.setSlug(newShareSlug());

        Team savedTeam = teamRepository.save(team);

        buildAndSavePokemon(savedTeam, dto.getPokemon());

        return savedTeam.getId();
    }

    /** Full-replacement update. Wipes the roster, re-applies scalar fields, rebuilds the roster. */
    @Transactional
    public Long updateTeam(@NotNull Long userId, @NotNull Long teamId, @Valid @NotNull TeamUpdateDto dto) {
        Team team = findTeamOrThrow(teamId);
        validateOwnership(team, userId);

        boolean wasPublic = Boolean.TRUE.equals(team.getIsPublic());

        deleteTeamPokemon(team);

        teamMapper.updateEntity(dto, team);
        rotateSlugIfMadePrivate(team, wasPublic);
        Team savedTeam = teamRepository.save(team);

        buildAndSavePokemon(savedTeam, dto.getPokemon());

        return savedTeam.getId();
    }

    /** Partial update of the team's scalar fields. Does not touch the roster. */
    @Transactional
    public Long patchTeam(@NotNull Long userId, @NotNull Long teamId, @Valid @NotNull TeamPatchDto dto) {
        Team team = findTeamOrThrow(teamId);
        validateOwnership(team, userId);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            team.setName(dto.getName());
        }
        if (dto.getIsPublic() != null) {
            boolean wasPublic = Boolean.TRUE.equals(team.getIsPublic());
            team.setIsPublic(dto.getIsPublic());
            rotateSlugIfMadePrivate(team, wasPublic);
        }

        teamRepository.save(team);
        return team.getId();
    }

    /** Owner-only. Replaces the team's share slug, so every link handed out so far stops working. */
    @Transactional
    public Long rotateShareSlug(@NotNull Long userId, @NotNull Long teamId) {
        Team team = findTeamOrThrow(teamId);
        validateOwnership(team, userId);

        team.setSlug(newShareSlug());

        teamRepository.save(team);
        return team.getId();
    }

    /** Owner-only delete. Cascades to roster + likes. */
    @Transactional
    public void deleteTeam(@NotNull Long userId, @NotNull Long teamId) {
        Team team = findTeamOrThrow(teamId);
        validateOwnership(team, userId);

        deleteTeamPokemon(team);
        deleteTeamLikes(team);
        teamRepository.delete(team);
    }

    /**
     * Admin delete. Defense-in-depth: re-checks the admin role even though the controller
     * already gates this with {@code @PreAuthorize}. Audit-logs the deletion with the team's
     * id, name, and owner for forensic clarity.
     *
     */
    @Transactional
    public void adminDeleteTeam(@NotNull Long adminId, @NotNull Long teamId) {
        Team team = findTeamOrThrow(teamId);

        AppUser adminUser = findUserOrThrow(adminId);

        if (!isUserAdmin(adminUser)) {
            throw new AccessDeniedException(
                    String.format("User '%s' is not an administrator", adminUser.getUsername()));
        }

        String teamDescription = String.format("id: %s, name: %s, owner: %s",
                team.getId(), team.getName(), team.getOwner().getUsername());
        String teamIdValue = team.getId().toString();

        deleteTeamPokemon(team);
        deleteTeamLikes(team);
        teamRepository.delete(team);

        auditLogCommandService.log(adminUser.getUsername(), AuditAction.ADMIN_TEAM_DELETE, ENTITY_NAME, teamIdValue, teamDescription);
    }

    /**
     * Like a public team. 409 if already liked. The denormalised counter is maintained by a
     * trigger on {@code team_like}, so every path that adds or removes a like row - this
     * one, cascades from deleting a user, the tombstone purge - keeps it in step.
     * Private teams answer 404 rather than 403, matching the read path, so a like attempt can't
     * be used to probe which team ids exist.
     */
    @Transactional
    public void likeTeam(@NotNull Long userId, @NotNull Long teamId) {
        AppUser user = findUserOrThrow(userId);
        Team team = findTeamOrThrow(teamId);

        if (!Boolean.TRUE.equals(team.getIsPublic())) {
            throw new ResourceNotFoundException(String.format("Team with id '%s' not found", teamId));
        }

        if (team.getOwner().getId().equals(user.getId())) {
            throw new ResourceAlreadyExistsException("A team cannot be liked by its owner");
        }

        TeamLikeId likeId = new TeamLikeId(user.getId(), teamId);

        if (teamLikeRepository.existsById(likeId)) {
            throw new ResourceAlreadyExistsException("Team already liked by this user");
        }

        TeamLike like = TeamLike.builder()
                .id(likeId)
                .user(user)
                .team(team)
                .build();
        teamLikeRepository.save(like);
    }

    /** Remove a like. 404 if the user hasn't liked the team. The counter follows via trigger. */
    @Transactional
    public void unlikeTeam(@NotNull Long userId, @NotNull Long teamId) {
        AppUser user = findUserOrThrow(userId);
        findTeamOrThrow(teamId);

        TeamLikeId likeId = new TeamLikeId(user.getId(), teamId);

        if (!teamLikeRepository.existsById(likeId)) {
            throw new ResourceNotFoundException("Team like not found");
        }

        teamLikeRepository.deleteById(likeId);
    }

    private void buildAndSavePokemon(Team team, List<TeamPokemonCreateDto> pokemonDtos) {
        for (int i = 0; i < pokemonDtos.size(); i++) {
            TeamPokemonCreateDto pokemonDto = pokemonDtos.get(i);

            validateLegality(pokemonDto);

            TeamPokemon teamPokemon = teamPokemonMapper.toEntity(pokemonDto);
            teamPokemon.setTeam(team);
            teamPokemon.setSlot(i + 1);
            teamPokemon.setPokemon(pokemonRepository.getReferenceById(pokemonDto.getPokemonId()));
            teamPokemon.setAbility(abilityRepository.getReferenceById(pokemonDto.getAbilityId()));

            if (pokemonDto.getNatureId() != null) {
                teamPokemon.setNature(natureRepository.getReferenceById(pokemonDto.getNatureId()));
            }
            if (pokemonDto.getItemId() != null) {
                teamPokemon.setHeldItem(itemRepository.getReferenceById(pokemonDto.getItemId()));
            }
            if (pokemonDto.getTeraTypeId() != null) {
                teamPokemon.setTeraType(typeRepository.getReferenceById(pokemonDto.getTeraTypeId()));
            }

            TeamPokemon savedTeamPokemon = teamPokemonRepository.save(teamPokemon);

            List<Integer> moveIds = pokemonDto.getMoveIds();
            for (int j = 0; j < moveIds.size(); j++) {
                TeamPokemonMoveId moveId = new TeamPokemonMoveId(savedTeamPokemon.getId(), j + 1);
                TeamPokemonMove teamPokemonMove = TeamPokemonMove.builder()
                        .id(moveId)
                        .teamPokemon(savedTeamPokemon)
                        .move(moveRepository.getReferenceById(moveIds.get(j)))
                        .build();
                teamPokemonMoveRepository.save(teamPokemonMove);
            }
        }
    }

    /**
     * Rejects a slot whose ability or moves the chosen form can't legally have.
     */
    private void validateLegality(TeamPokemonCreateDto dto) {
        Integer pokemonId = dto.getPokemonId();

        if (!pokemonAbilityRepository.existsByIdPokemonIdAndIdAbilityId(pokemonId, dto.getAbilityId())) {
            throw new IllegalTeamCompositionException(
                    String.format("Ability %s is not available to Pokemon %s", dto.getAbilityId(), pokemonId));
        }

        List<Integer> requested = dto.getMoveIds();

        if (Set.copyOf(requested).size() != requested.size()) {
            throw new IllegalTeamCompositionException(
                    String.format("Pokemon %s cannot know the same move twice", pokemonId));
        }

        Set<Integer> learnable = Set.copyOf(pokemonMoveRepository.findLearnableMoveIds(pokemonId, requested));

        for (Integer moveId : requested) {
            if (!learnable.contains(moveId)) {
                throw new IllegalTeamCompositionException(
                        String.format("Move %s is not learnable by Pokemon %s", moveId, pokemonId));
            }
        }
    }

    private void rotateSlugIfMadePrivate(Team team, boolean wasPublic) {
        if (wasPublic && !Boolean.TRUE.equals(team.getIsPublic())) {
            team.setSlug(newShareSlug());
        }
    }

    private static String newShareSlug() {
        return UUID.randomUUID().toString();
    }

    private void deleteTeamPokemon(Team team) {
        teamPokemonMoveRepository.deleteByTeamPokemonTeam(team);
        teamPokemonRepository.deleteByTeam(team);
    }

    private void deleteTeamLikes(Team team) {
        teamLikeRepository.deleteByTeam(team);
    }

    private void validateOwnership(Team team, Long userId) {
        if (!team.getOwner().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    String.format("Team with id '%s' not found", team.getId()));
        }
    }

    private boolean isUserAdmin(AppUser user) {
        return UserRole.ADMIN.equals(user.getRole());
    }

    private AppUser findUserOrThrow(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with id '%s' not found", userId)));
    }

    private Team findTeamOrThrow(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Team with id '%s' not found", teamId)));
    }
}
