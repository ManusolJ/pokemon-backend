package com.poketeambuilder.services.command;

import java.util.List;
import java.util.UUID;

import com.poketeambuilder.infrastructure.exceptions.ResourceNotFoundException;
import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;

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
import com.poketeambuilder.repositories.TeamPokemonRepository;
import com.poketeambuilder.repositories.TeamPokemonMoveRepository;

import com.poketeambuilder.utils.enums.UserRole;
import com.poketeambuilder.utils.enums.AuditAction;
import com.poketeambuilder.utils.enums.PokemonGender;

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

    private final AuditLogCommandService auditLogCommandService;

    /** Creates a team owned by the authenticated user with a fresh share slug. */
    @Transactional
    public Long createTeam(@NotNull String username, @Valid @NotNull TeamCreateDto dto) {
        AppUser owner = findUserOrThrow(username);

        Team team = teamMapper.toEntity(dto);
        team.setOwner(owner);
        team.setSlug(UUID.randomUUID().toString());

        Team savedTeam = teamRepository.save(team);

        buildAndSavePokemon(savedTeam, dto.getPokemon());

        return savedTeam.getId();
    }

    /** Full-replacement update. Wipes the roster, re-applies scalar fields, rebuilds the roster. */
    @Transactional
    public Long updateTeam(@NotNull String username, @NotNull Long teamId, @Valid @NotNull TeamUpdateDto dto) {
        Team team = findTeamOrThrow(teamId);
        validateOwnership(team, username);

        deleteTeamPokemon(team);

        teamMapper.updateEntity(dto, team);
        Team savedTeam = teamRepository.save(team);

        buildAndSavePokemon(savedTeam, dto.getPokemon());

        return savedTeam.getId();
    }

    /** Partial update of the team's scalar fields. Does not touch the roster. */
    @Transactional
    public Long patchTeam(@NotNull String username, @NotNull Long teamId, @Valid @NotNull TeamPatchDto dto) {
        Team team = findTeamOrThrow(teamId);
        validateOwnership(team, username);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            team.setName(dto.getName());
        }
        if (dto.getIsPublic() != null) {
            team.setIsPublic(dto.getIsPublic());
        }

        teamRepository.save(team);
        return team.getId();
    }

    /** Owner-only delete. Cascades to roster + likes. */
    @Transactional
    public void deleteTeam(@NotNull String username, @NotNull Long teamId) {
        Team team = findTeamOrThrow(teamId);
        validateOwnership(team, username);

        deleteTeamPokemon(team);
        deleteTeamLikes(team);
        teamRepository.delete(team);
    }

    /**
     * Admin delete. Defense-in-depth: re-checks the admin role even though the controller
     * already gates this with {@code @PreAuthorize}. Audit-logs the deletion with the team's
     * id, name, and owner for forensic clarity.
     */
    @Transactional
    public void adminDeleteTeam(@NotNull String adminUsername, @NotNull Long teamId) {
        Team team = findTeamOrThrow(teamId);

        AppUser adminUser = findUserOrThrow(adminUsername);

        if (!isUserAdmin(adminUser)) {
            throw new ResourceNotFoundException(
                    String.format("Team with id '%s' not found for user '%s'", team.getId(), adminUsername));
        }

        deleteTeamPokemon(team);
        deleteTeamLikes(team);
        teamRepository.delete(team);

        String entityDescription = String.format("%s (id: %s, name: %s, owner: %s)",
                ENTITY_NAME, team.getId(), team.getName(), team.getOwner().getUsername());

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_TEAM_DELETE, entityDescription, team.getId().toString());
    }

    /** Like a public team. 409 if already liked. Increments the denormalised counter atomically. */
    @Transactional
    public void likeTeam(@NotNull String username, @NotNull Long teamId) {
        AppUser user = findUserOrThrow(username);
        Team team = findTeamOrThrow(teamId);

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
        teamRepository.incrementLikeCount(teamId);
    }

    /** Remove a like. 404 if the user hasn't liked the team. Decrements the counter. */
    @Transactional
    public void unlikeTeam(@NotNull String username, @NotNull Long teamId) {
        AppUser user = findUserOrThrow(username);
        findTeamOrThrow(teamId);

        TeamLikeId likeId = new TeamLikeId(user.getId(), teamId);

        if (!teamLikeRepository.existsById(likeId)) {
            throw new ResourceNotFoundException("Team like not found");
        }

        teamLikeRepository.deleteById(likeId);
        teamRepository.decrementLikeCount(teamId);
    }

    private void buildAndSavePokemon(Team team, List<TeamPokemonCreateDto> pokemonDtos) {
        for (int i = 0; i < pokemonDtos.size(); i++) {
            TeamPokemonCreateDto pokemonDto = pokemonDtos.get(i);

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
            if (pokemonDto.getGender() != null) {
                teamPokemon.setGender(PokemonGender.fromValue(pokemonDto.getGender()));
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

    private void deleteTeamPokemon(Team team) {
        teamPokemonMoveRepository.deleteByTeamPokemonTeam(team);
        teamPokemonRepository.deleteByTeam(team);
    }

    private void deleteTeamLikes(Team team) {
        teamLikeRepository.deleteByTeam(team);
    }

    private void validateOwnership(Team team, String username) {
        if (!team.getOwner().getUsername().equals(username)) {
            throw new ResourceNotFoundException(
                    String.format("Team with id '%s' not found for user '%s'", team.getId(), username));
        }
    }

    private boolean isUserAdmin(AppUser user) {
        return UserRole.ADMIN.equals(user.getRole());
    }

    private AppUser findUserOrThrow(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User '%s' not found", username)));
    }

    private Team findTeamOrThrow(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Team with id '%s' not found", teamId)));
    }
}
