package com.poketeambuilder.services.command;

import java.util.List;
import java.util.Optional;

import com.poketeambuilder.dtos.front.team.details.TeamCreateDto;
import com.poketeambuilder.dtos.front.team.roster.TeamPokemonCreateDto;

import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.Team;

import com.poketeambuilder.infrastructure.exceptions.ResourceNotFoundException;
import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;
import com.poketeambuilder.infrastructure.exceptions.IllegalTeamCompositionException;

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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Team mutations. Ownership failures answer 404 rather than 403 throughout, so team ids can't
 * be probed; likes are limited to other people's public teams; and a roster is rejected when it
 * assigns an ability or move the chosen form cannot have, which the foreign keys alone don't
 * catch.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TeamCommandServiceTest {

    private static final long OWNER_ID = 1L;
    private static final long STRANGER_ID = 2L;
    private static final long TEAM_ID = 10L;
    private static final int PIKACHU = 25;
    private static final int STATIC_ABILITY = 9;
    private static final int THUNDERBOLT = 85;

    @Mock private TeamMapper teamMapper;
    @Mock private TeamPokemonMapper teamPokemonMapper;
    @Mock private UserRepository userRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TeamLikeRepository teamLikeRepository;
    @Mock private TeamPokemonRepository teamPokemonRepository;
    @Mock private TeamPokemonMoveRepository teamPokemonMoveRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private TypeRepository typeRepository;
    @Mock private MoveRepository moveRepository;
    @Mock private NatureRepository natureRepository;
    @Mock private AbilityRepository abilityRepository;
    @Mock private PokemonRepository pokemonRepository;
    @Mock private PokemonMoveRepository pokemonMoveRepository;
    @Mock private PokemonAbilityRepository pokemonAbilityRepository;
    @Mock private AuditLogCommandService auditLogCommandService;

    @InjectMocks private TeamCommandService teamCommandService;

    private AppUser owner;
    private AppUser stranger;

    @BeforeEach
    void setUp() {
        owner = user(OWNER_ID, "ash");
        stranger = user(STRANGER_ID, "gary");

        when(userRepository.findByUsernameAndDeletedAtIsNull("ash")).thenReturn(Optional.of(owner));
        when(userRepository.findByUsernameAndDeletedAtIsNull("gary")).thenReturn(Optional.of(stranger));
    }

    // --- ownership -----------------------------------------------------------------------

    @Test
    @DisplayName("Deleting someone else's team answers 404, not 403")
    void deletingAnotherUsersTeamLooksLikeItDoesNotExist() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));

        assertThatThrownBy(() -> teamCommandService.deleteTeam("gary", TEAM_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(teamRepository, never()).delete(any(Team.class));
    }

    @Test
    @DisplayName("A missing team is reported the same way as one owned by someone else")
    void missingTeamAndForeignTeamAreIndistinguishable() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamCommandService.deleteTeam("gary", TEAM_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- likes ---------------------------------------------------------------------------

    @Test
    @DisplayName("A private team cannot be liked, and refuses as if it did not exist")
    void privateTeamsCannotBeLiked() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, false)));

        assertThatThrownBy(() -> teamCommandService.likeTeam("gary", TEAM_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(teamLikeRepository, never()).save(any());
        verify(teamRepository, never()).incrementLikeCount(anyLong());
    }

    @Test
    @DisplayName("An owner cannot like their own team")
    void ownersCannotLikeTheirOwnTeam() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));

        assertThatThrownBy(() -> teamCommandService.likeTeam("ash", TEAM_ID))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(teamRepository, never()).incrementLikeCount(anyLong());
    }

    @Test
    @DisplayName("Liking twice is refused, and the counter moves only once")
    void likingTwiceIsRefused() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));
        when(teamLikeRepository.existsById(any())).thenReturn(true);

        assertThatThrownBy(() -> teamCommandService.likeTeam("gary", TEAM_ID))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(teamRepository, never()).incrementLikeCount(anyLong());
    }

    @Test
    @DisplayName("Liking another user's public team records it and bumps the counter")
    void likingAPublicTeamSucceeds() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));
        when(teamLikeRepository.existsById(any())).thenReturn(false);

        teamCommandService.likeTeam("gary", TEAM_ID);

        verify(teamLikeRepository).save(any());
        verify(teamRepository).incrementLikeCount(TEAM_ID);
    }

    @Test
    @DisplayName("Removing a like that was never given is refused")
    void unlikingWithoutALikeIsRefused() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));
        when(teamLikeRepository.existsById(any())).thenReturn(false);

        assertThatThrownBy(() -> teamCommandService.unlikeTeam("gary", TEAM_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(teamRepository, never()).decrementLikeCount(anyLong());
    }

    // --- roster legality -------------------------------------------------------------------

    @Test
    @DisplayName("An ability the form cannot have is rejected before anything is written")
    void rejectsAnAbilityTheFormCannotHave() {
        when(pokemonAbilityRepository.existsByIdPokemonIdAndIdAbilityId(PIKACHU, STATIC_ABILITY))
                .thenReturn(false);

        assertThatThrownBy(() -> teamCommandService.createTeam("ash", createTeam()))
                .isInstanceOf(IllegalTeamCompositionException.class)
                .hasMessageContaining("Ability");

        verify(teamPokemonRepository, never()).save(any());
    }

    @Test
    @DisplayName("A move the form cannot learn is rejected")
    void rejectsAMoveTheFormCannotLearn() {
        when(pokemonAbilityRepository.existsByIdPokemonIdAndIdAbilityId(PIKACHU, STATIC_ABILITY))
                .thenReturn(true);
        when(pokemonMoveRepository.findLearnableMoveIds(anyInt(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> teamCommandService.createTeam("ash", createTeam()))
                .isInstanceOf(IllegalTeamCompositionException.class)
                .hasMessageContaining("Move");

        verify(teamPokemonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Legality is checked against the join tables, not merely that the rows exist")
    void checksLegalityAgainstTheJoinTables() {
        when(pokemonAbilityRepository.existsByIdPokemonIdAndIdAbilityId(PIKACHU, STATIC_ABILITY))
                .thenReturn(true);
        when(pokemonMoveRepository.findLearnableMoveIds(anyInt(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> teamCommandService.createTeam("ash", createTeam()))
                .isInstanceOf(IllegalTeamCompositionException.class);

        verify(pokemonAbilityRepository).existsByIdPokemonIdAndIdAbilityId(PIKACHU, STATIC_ABILITY);
        verify(pokemonMoveRepository).findLearnableMoveIds(PIKACHU, List.of(THUNDERBOLT));
    }

    // --- helpers ---------------------------------------------------------------------------

    private AppUser user(long id, String username) {
        return AppUser.builder()
                .id(id)
                .username(username)
                .email(username + "@test.local")
                .password("hashed")
                .role(UserRole.USER)
                .enabled(true)
                .build();
    }

    private Team team(AppUser teamOwner, boolean isPublic) {
        return Team.builder()
                .id(TEAM_ID)
                .owner(teamOwner)
                .name("Kanto")
                .isPublic(isPublic)
                .likeCount(0)
                .build();
    }

    private TeamCreateDto createTeam() {
        TeamPokemonCreateDto member = new TeamPokemonCreateDto();
        ReflectionTestUtils.setField(member, "pokemonId", PIKACHU);
        ReflectionTestUtils.setField(member, "abilityId", STATIC_ABILITY);
        ReflectionTestUtils.setField(member, "moveIds", List.of(THUNDERBOLT));

        TeamCreateDto dto = new TeamCreateDto();
        ReflectionTestUtils.setField(dto, "name", "Kanto");
        ReflectionTestUtils.setField(dto, "isPublic", false);
        ReflectionTestUtils.setField(dto, "pokemon", List.of(member));

        when(teamMapper.toEntity(any(TeamCreateDto.class))).thenReturn(team(owner, false));
        when(teamRepository.save(any(Team.class))).thenAnswer(call -> call.getArgument(0));

        return dto;
    }
}
