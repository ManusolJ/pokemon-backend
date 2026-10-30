package com.poketeambuilder.services.command;

import java.util.List;
import java.util.Optional;

import com.poketeambuilder.dtos.front.team.details.TeamCreateDto;
import com.poketeambuilder.dtos.front.team.details.TeamPatchDto;
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

import com.poketeambuilder.utils.enums.AuditAction;
import com.poketeambuilder.utils.enums.UserRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
class TeamCommandServiceTest {

    private static final long OWNER_ID = 1L;
    private static final long STRANGER_ID = 2L;
    private static final long TEAM_ID = 10L;
    private static final int PIKACHU = 25;
    private static final int STATIC_ABILITY = 9;
    private static final int THUNDERBOLT = 85;
    private static final String OLD_SLUG = "00000000-0000-4000-8000-000000000000";

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

    @Captor private ArgumentCaptor<String> auditEntity;
    @Captor private ArgumentCaptor<String> auditDetails;

    private AppUser owner;
    private AppUser stranger;

    @BeforeEach
    void setUp() {
        owner = user(OWNER_ID, "ash");
        stranger = user(STRANGER_ID, "gary");

        lenient().when(userRepository.findByIdAndDeletedAtIsNull(OWNER_ID)).thenReturn(Optional.of(owner));
        lenient().when(userRepository.findByIdAndDeletedAtIsNull(STRANGER_ID)).thenReturn(Optional.of(stranger));
        lenient().when(teamMapper.toEntity(any(TeamCreateDto.class))).thenReturn(team(owner, false));
        lenient().when(teamRepository.save(any(Team.class))).thenAnswer(call -> call.getArgument(0));
    }

    // --- ownership -----------------------------------------------------------------------

    @Test
    @DisplayName("Deleting someone else's team answers 404, not 403")
    void deletingAnotherUsersTeamLooksLikeItDoesNotExist() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));

        assertThatThrownBy(() -> teamCommandService.deleteTeam(STRANGER_ID, TEAM_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(teamRepository, never()).delete(any(Team.class));
    }

    @Test
    @DisplayName("A missing team is reported the same way as one owned by someone else")
    void missingTeamAndForeignTeamAreIndistinguishable() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamCommandService.deleteTeam(STRANGER_ID, TEAM_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- likes ---------------------------------------------------------------------------

    @Test
    @DisplayName("A private team cannot be liked, and refuses as if it did not exist")
    void privateTeamsCannotBeLiked() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, false)));

        assertThatThrownBy(() -> teamCommandService.likeTeam(STRANGER_ID, TEAM_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(teamLikeRepository, never()).save(any());
    }

    @Test
    @DisplayName("An owner cannot like their own team")
    void ownersCannotLikeTheirOwnTeam() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));

        assertThatThrownBy(() -> teamCommandService.likeTeam(OWNER_ID, TEAM_ID))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(teamLikeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Liking twice is refused, and no second like row is written")
    void likingTwiceIsRefused() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));
        when(teamLikeRepository.existsById(any())).thenReturn(true);

        assertThatThrownBy(() -> teamCommandService.likeTeam(STRANGER_ID, TEAM_ID))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(teamLikeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Liking another user's public team records it (the counter follows by trigger)")
    void likingAPublicTeamSucceeds() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));
        when(teamLikeRepository.existsById(any())).thenReturn(false);

        teamCommandService.likeTeam(STRANGER_ID, TEAM_ID);

        verify(teamLikeRepository).save(any());
    }

    @Test
    @DisplayName("Removing a like that was never given is refused")
    void unlikingWithoutALikeIsRefused() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));
        when(teamLikeRepository.existsById(any())).thenReturn(false);

        assertThatThrownBy(() -> teamCommandService.unlikeTeam(STRANGER_ID, TEAM_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(teamLikeRepository, never()).deleteById(any());
    }

    // --- admin moderation ------------------------------------------------------------------

    @Test
    @DisplayName("An admin delete records the type in entity and the description in details")
    void adminDeleteKeepsTheDescriptionOutOfTheReferenceColumn() {
        AppUser oak = user(3L, "oak");
        oak.setRole(UserRole.ADMIN);
        when(userRepository.findByIdAndDeletedAtIsNull(3L)).thenReturn(Optional.of(oak));
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));

        teamCommandService.adminDeleteTeam(3L, TEAM_ID);

        verify(auditLogCommandService).log(eq("oak"), eq(AuditAction.ADMIN_TEAM_DELETE),
                auditEntity.capture(), eq(String.valueOf(TEAM_ID)), auditDetails.capture());

        assertThat(auditEntity.getValue()).isEqualTo("Team");
        assertThat(auditEntity.getValue().length()).isLessThanOrEqualTo(50);
        assertThat(auditDetails.getValue()).contains("Kanto").contains("ash");
    }

    @Test
    @DisplayName("A non-admin reaching the admin delete is refused as forbidden, not as missing")
    void adminDeleteRefusesANonAdmin() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));

        assertThatThrownBy(() -> teamCommandService.adminDeleteTeam(STRANGER_ID, TEAM_ID))
                .isInstanceOf(AccessDeniedException.class);

        verify(teamRepository, never()).delete(any(Team.class));
    }

    // --- roster legality -------------------------------------------------------------------

    @Test
    @DisplayName("An ability the form cannot have is rejected before anything is written")
    void rejectsAnAbilityTheFormCannotHave() {
        when(pokemonAbilityRepository.existsByIdPokemonIdAndIdAbilityId(PIKACHU, STATIC_ABILITY))
                .thenReturn(false);

        assertThatThrownBy(() -> teamCommandService.createTeam(OWNER_ID, createTeam()))
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

        assertThatThrownBy(() -> teamCommandService.createTeam(OWNER_ID, createTeam()))
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

        assertThatThrownBy(() -> teamCommandService.createTeam(OWNER_ID, createTeam()))
                .isInstanceOf(IllegalTeamCompositionException.class);

        verify(pokemonAbilityRepository).existsByIdPokemonIdAndIdAbilityId(PIKACHU, STATIC_ABILITY);
        verify(pokemonMoveRepository).findLearnableMoveIds(PIKACHU, List.of(THUNDERBOLT));
    }

    @Test
    @DisplayName("A Pokemon that lists the same move twice is rejected")
    void rejectsDuplicateMoves() {
        when(pokemonAbilityRepository.existsByIdPokemonIdAndIdAbilityId(PIKACHU, STATIC_ABILITY))
                .thenReturn(true);

        TeamCreateDto dto = createTeam();
        ReflectionTestUtils.setField(dto.getPokemon().get(0), "moveIds", List.of(THUNDERBOLT, THUNDERBOLT));

        assertThatThrownBy(() -> teamCommandService.createTeam(OWNER_ID, dto))
                .isInstanceOf(IllegalTeamCompositionException.class)
                .hasMessageContaining("same move");

        verify(teamPokemonRepository, never()).save(any());
    }

    // --- ownership by id ---------------------------------------------------------------------

    @Test
    @DisplayName("Ownership is decided by user id, so an account that took over the owner's old name has no access")
    void ownershipFollowsTheIdNotTheName() {
        // Id 99 now holds the owner's old name "ash"; the team still belongs to id 1.
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(owner, true)));

        assertThatThrownBy(() -> teamCommandService.deleteTeam(99L, TEAM_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(teamRepository, never()).delete(any(Team.class));
    }

    // --- share links ---------------------------------------------------------------------------

    @Test
    @DisplayName("Rotating the share link gives the team a new slug")
    void ownerCanRotateTheShareLink() {
        Team team = team(owner, false);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));

        teamCommandService.rotateShareSlug(OWNER_ID, TEAM_ID);

        assertThat(team.getSlug()).isNotNull().isNotEqualTo(OLD_SLUG);
        verify(teamRepository).save(team);
    }

    @Test
    @DisplayName("Only the owner can rotate a share link, and anyone else gets the usual 404")
    void strangersCannotRotateTheShareLink() {
        Team team = team(owner, false);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamCommandService.rotateShareSlug(STRANGER_ID, TEAM_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThat(team.getSlug()).isEqualTo(OLD_SLUG);
    }

    @Test
    @DisplayName("Making a public team private retires the slug that was on show in the public listing")
    void goingPrivateRotatesTheSlug() {
        Team team = team(owner, true);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));

        teamCommandService.patchTeam(OWNER_ID, TEAM_ID, patch(false));

        assertThat(team.getIsPublic()).isFalse();
        assertThat(team.getSlug()).isNotEqualTo(OLD_SLUG);
    }

    @Test
    @DisplayName("Making a private team public keeps its share link working")
    void goingPublicKeepsTheSlug() {
        Team team = team(owner, false);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));

        teamCommandService.patchTeam(OWNER_ID, TEAM_ID, patch(true));

        assertThat(team.getSlug()).isEqualTo(OLD_SLUG);
    }

    @Test
    @DisplayName("A patch that leaves a private team private keeps its share link working")
    void stayingPrivateKeepsTheSlug() {
        Team team = team(owner, false);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));

        teamCommandService.patchTeam(OWNER_ID, TEAM_ID, patch(false));

        assertThat(team.getSlug()).isEqualTo(OLD_SLUG);
    }

    // --- helpers ---------------------------------------------------------------------------

    private TeamPatchDto patch(boolean isPublic) {
        TeamPatchDto dto = new TeamPatchDto();
        ReflectionTestUtils.setField(dto, "isPublic", isPublic);
        return dto;
    }


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
                .slug(OLD_SLUG)
                .likeCount(0)
                .build();
    }

    /** Builds a payload. Deliberately free of stubbing, so a caller can see what it is arranging. */
    private TeamCreateDto createTeam() {
        TeamPokemonCreateDto member = new TeamPokemonCreateDto();
        ReflectionTestUtils.setField(member, "pokemonId", PIKACHU);
        ReflectionTestUtils.setField(member, "abilityId", STATIC_ABILITY);
        ReflectionTestUtils.setField(member, "moveIds", List.of(THUNDERBOLT));

        TeamCreateDto dto = new TeamCreateDto();
        ReflectionTestUtils.setField(dto, "name", "Kanto");
        ReflectionTestUtils.setField(dto, "isPublic", false);
        ReflectionTestUtils.setField(dto, "pokemon", List.of(member));

        return dto;
    }
}
