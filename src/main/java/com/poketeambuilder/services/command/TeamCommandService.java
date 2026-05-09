package com.poketeambuilder.services.command;

import com.poketeambuilder.infrastructure.exceptions.ResourceNotFoundException;
import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;

import com.poketeambuilder.dtos.front.team.team.TeamCreateDto;
import com.poketeambuilder.dtos.front.team.team.TeamUpdateDto;
import com.poketeambuilder.dtos.front.team.pokemon.TeamPokemonCreateDto;

import com.poketeambuilder.entities.Item;
import com.poketeambuilder.entities.Move;
import com.poketeambuilder.entities.Team;
import com.poketeambuilder.entities.Type;
import com.poketeambuilder.entities.Nature;
import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.Ability;
import com.poketeambuilder.entities.Pokemon;
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
import com.poketeambuilder.utils.enums.AuditAction;
import com.poketeambuilder.utils.enums.PokemonGender;
import com.poketeambuilder.utils.enums.UserRole;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class TeamCommandService {

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

    private final static String ENTITY_NAME = "Team";

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

    @Transactional
    public void deleteTeam(@NotNull String username, @NotNull Long teamId) {
        Team team = findTeamOrThrow(teamId);
        validateOwnership(team, username);

        deleteTeamPokemon(team);
        deleteTeamLikes(team);
        teamRepository.delete(team);
    }

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

        String entityName = String.format(ENTITY_NAME + " (id: %s, name: %s, owner: %s)", team.getId(), team.getName(), team.getOwner().getUsername());

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_TEAM_DELETE, entityName, team.getId().toString());
    }

    @Transactional
    public void likeTeam(@NotNull String username, @NotNull Long teamId) {
        AppUser user = findUserOrThrow(username);
        Team team = findTeamOrThrow(teamId);

        TeamLikeId likeId = new TeamLikeId(teamId, user.getId());

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

    @Transactional
    public void unlikeTeam(@NotNull String username, @NotNull Long teamId) {
        AppUser user = findUserOrThrow(username);
        findTeamOrThrow(teamId);

        TeamLikeId likeId = new TeamLikeId(teamId, user.getId());

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
            teamPokemon.setPokemon(findPokemonOrThrow(pokemonDto.getPokemonId()));
            teamPokemon.setAbility(findAbilityOrThrow(pokemonDto.getAbilityId()));

            if (pokemonDto.getNatureId() != null) {
                teamPokemon.setNature(findNatureOrThrow(pokemonDto.getNatureId()));
            }
            if (pokemonDto.getItemId() != null) {
                teamPokemon.setHeldItem(findItemOrThrow(pokemonDto.getItemId()));
            }
            if (pokemonDto.getTeraTypeId() != null) {
                teamPokemon.setTeraType(findTypeOrThrow(pokemonDto.getTeraTypeId()));
            }
            if (pokemonDto.getGender() != null) {
                teamPokemon.setGender(PokemonGender.fromValue(pokemonDto.getGender()));
            }

            TeamPokemon savedTeamPokemon = teamPokemonRepository.save(teamPokemon);

            List<Integer> moveIds = pokemonDto.getMoveIds();
            for (int j = 0; j < moveIds.size(); j++) {
                Move move = findMoveOrThrow(moveIds.get(j));
                TeamPokemonMoveId moveId = new TeamPokemonMoveId(savedTeamPokemon.getId(), j + 1);
                TeamPokemonMove teamPokemonMove = TeamPokemonMove.builder()
                        .id(moveId)
                        .teamPokemon(savedTeamPokemon)
                        .move(move)
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
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User '%s' not found", username)));
    }

    private Team findTeamOrThrow(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Team with id '%s' not found", teamId)));
    }

    private Pokemon findPokemonOrThrow(Integer pokemonId) {
        return pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Pokemon with id '%s' not found", pokemonId)));
    }

    private Ability findAbilityOrThrow(Integer abilityId) {
        return abilityRepository.findById(abilityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Ability with id '%s' not found", abilityId)));
    }

    private Nature findNatureOrThrow(Integer natureId) {
        return natureRepository.findById(natureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Nature with id '%s' not found", natureId)));
    }

    private Item findItemOrThrow(Integer itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Item with id '%s' not found", itemId)));
    }

    private Type findTypeOrThrow(Integer typeId) {
        return typeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Type with id '%s' not found", typeId)));
    }

    private Move findMoveOrThrow(Integer moveId) {
        return moveRepository.findById(moveId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Move with id '%s' not found", moveId)));
    }
}
