package com.poketeambuilder.integration;

import java.util.List;
import java.time.Instant;

import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.SeedLog;

import com.poketeambuilder.repositories.SeedLogRepository;
import com.poketeambuilder.repositories.TeamRepository;
import com.poketeambuilder.repositories.UserRepository;
import com.poketeambuilder.repositories.PokemonMoveRepository;
import com.poketeambuilder.repositories.PokemonAbilityRepository;

import com.poketeambuilder.utils.enums.SeedStatus;
import com.poketeambuilder.utils.enums.UserRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the derived and hand-written queries.
 *
 */
class RepositoryQueriesIntegrationTest extends PostgresTestBase {

    @Autowired private EntityManager em;
    @Autowired private UserRepository userRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private SeedLogRepository seedLogRepository;
    @Autowired private PokemonMoveRepository pokemonMoveRepository;
    @Autowired private PokemonAbilityRepository pokemonAbilityRepository;

    // --- last-administrator count ---------------------------------------------------------

    @Test
    @DisplayName("Counts only administrators who could actually sign in")
    void countsSignedInCapableAdminsOnly() {
        persist(admin("oak", true, null));
        persist(admin("elm", false, null));
        persist(admin("birch", true, Instant.now()));
        persist(regular("ash"));
        em.flush();

        assertThat(userRepository.countByRoleAndEnabledTrueAndDeletedAtIsNull(UserRole.ADMIN)).isEqualTo(1);
        assertThat(userRepository.countByRoleAndEnabledTrueAndDeletedAtIsNull(UserRole.USER)).isEqualTo(1);
    }

    // --- public team visibility -------------------------------------------------------------

    @Test
    @DisplayName("A team whose owner is tombstoned stops counting as visible")
    void teamsOfTombstonedOwnersAreNotVisible() {
        long live = givenATeamOwnedBy("ash", null);
        long orphaned = givenATeamOwnedBy("gary", Instant.now());
        em.flush();

        assertThat(teamRepository.existsByIdAndOwnerDeletedAtIsNull(live)).isTrue();
        assertThat(teamRepository.existsByIdAndOwnerDeletedAtIsNull(orphaned)).isFalse();
    }

    // --- roster legality ----------------------------------------------------------------------

    @Test
    @DisplayName("Ability legality resolves through the composite id")
    void abilityLegalityResolvesThroughTheCompositeId() {
        givenPikachuWithStaticAndThunderbolt();

        assertThat(pokemonAbilityRepository.existsByIdPokemonIdAndIdAbilityId(25, 9)).isTrue();
        assertThat(pokemonAbilityRepository.existsByIdPokemonIdAndIdAbilityId(25, 999)).isFalse();
    }

    @Test
    @DisplayName("Learnable moves come back de-duplicated across learn methods")
    void learnableMovesAreDistinct() {
        givenPikachuWithStaticAndThunderbolt();

        List<Integer> learnable = pokemonMoveRepository.findLearnableMoveIds(25, List.of(85, 999));

        assertThat(learnable).containsExactly(85);
    }

    @Test
    @DisplayName("A form that learns nothing requested returns empty rather than null")
    void unknownMovesReturnEmpty() {
        givenPikachuWithStaticAndThunderbolt();

        assertThat(pokemonMoveRepository.findLearnableMoveIds(25, List.of(999))).isEmpty();
    }

    // --- startup reconciliation ------------------------------------------------------------------

    @Test
    @DisplayName("Interrupted runs are closed off and stamped, finished ones left alone")
    void failRunningLogsClosesOnlyTheRunningOne() {
        SeedLog completed = seedLogRepository.save(SeedLog.builder().status(SeedStatus.COMPLETED).build());
        SeedLog running = seedLogRepository.save(SeedLog.builder().status(SeedStatus.RUNNING).build());
        em.flush();

        int reconciled = seedLogRepository.failRunningLogs(SeedStatus.FAILED, SeedStatus.RUNNING);
        em.clear();

        assertThat(reconciled).isEqualTo(1);
        assertThat(seedLogRepository.findById(running.getId()))
                .get()
                .satisfies(log -> {
                    assertThat(log.getStatus()).isEqualTo(SeedStatus.FAILED);
                    assertThat(log.getCompletedAt()).isNotNull();
                });
        assertThat(seedLogRepository.findById(completed.getId()))
                .get()
                .extracting(SeedLog::getStatus)
                .isEqualTo(SeedStatus.COMPLETED);
    }

    @Test
    @DisplayName("With nothing running, reconciliation is a no-op")
    void reconciliationIsANoOpWhenNothingIsRunning() {
        seedLogRepository.save(SeedLog.builder().status(SeedStatus.COMPLETED).build());
        em.flush();

        assertThat(seedLogRepository.failRunningLogs(SeedStatus.FAILED, SeedStatus.RUNNING)).isZero();
    }

    // --- fixtures ---------------------------------------------------------------------------------

    private AppUser admin(String username, boolean enabled, Instant deletedAt) {
        AppUser user = AppUser.builder()
                .username(username)
                .email(username + "@test.local")
                .password("hash")
                .role(UserRole.ADMIN)
                .enabled(enabled)
                .build();
        user.setDeletedAt(deletedAt);
        return user;
    }

    private AppUser regular(String username) {
        return AppUser.builder()
                .username(username)
                .email(username + "@test.local")
                .password("hash")
                .role(UserRole.USER)
                .enabled(true)
                .build();
    }

    private void persist(AppUser user) {
        em.persist(user);
    }

    private long givenATeamOwnedBy(String username, Instant deletedAt) {
        AppUser owner = regular(username);
        owner.setDeletedAt(deletedAt);
        em.persist(owner);
        em.flush();

        em.createNativeQuery("INSERT INTO team (user_id, name) VALUES (:owner, :name)")
                .setParameter("owner", owner.getId())
                .setParameter("name", username + "'s team")
                .executeUpdate();

        return ((Number) em.createNativeQuery(
                        "SELECT id FROM team WHERE user_id = :owner")
                .setParameter("owner", owner.getId())
                .getSingleResult()).longValue();
    }

    private void givenPikachuWithStaticAndThunderbolt() {
        native_("INSERT INTO type (id, name) VALUES (13, 'electric')");
        native_("INSERT INTO ability (id, name) VALUES (9, 'static')");
        native_("INSERT INTO move (id, name, type_id, category) VALUES (85, 'thunderbolt', 13, 'special')");
        native_("INSERT INTO pokemon_species (id, name, genus, national_dex_number, generation) VALUES (25, 'pikachu', 'Mouse', 25, 1)");
        native_("""
                INSERT INTO pokemon (id, name, primary_type_id, species_id,
                                     base_hp, base_atk, base_def, base_sp_atk, base_sp_def, base_speed)
                VALUES (25, 'pikachu', 13, 25, 35, 55, 40, 50, 50, 90)
                """);
        native_("INSERT INTO pokemon_ability (pokemon_id, ability_id, slot, is_hidden) VALUES (25, 9, 1, false)");
        native_("INSERT INTO pokemon_move (pokemon_id, move_id, learn_method, level_learned_at) VALUES (25, 85, 'level-up', 26)");
        native_("INSERT INTO pokemon_move (pokemon_id, move_id, learn_method, level_learned_at) VALUES (25, 85, 'machine', null)");
        em.flush();
    }

    private void native_(String sql) {
        em.createNativeQuery(sql).executeUpdate();
    }
}
