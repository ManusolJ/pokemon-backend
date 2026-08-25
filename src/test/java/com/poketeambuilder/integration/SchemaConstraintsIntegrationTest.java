package com.poketeambuilder.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Schema constraints.
 *
 * <p>Each method asserts a single violation. Postgres checks these at statement time and a
 * failed statement poisons the surrounding transaction, so a second assertion after one would
 * report the aborted transaction instead of the constraint.</p>
 */
class SchemaConstraintsIntegrationTest extends PostgresTestBase {

    @Autowired private EntityManager em;

    // --- catalogue rows referenced by user data ------------------------------------------

    @Test
    @DisplayName("A Pokemon a team references cannot be deleted")
    void catalogueRowsReferencedByATeamAreProtected() {
        givenATeamWithOnePokemon();

        assertThatThrownBy(() -> execute("DELETE FROM pokemon"))
                .hasStackTraceContaining("team_pokemon");
    }

    @Test
    @DisplayName("An unreferenced Pokemon can still be deleted")
    void unreferencedCatalogueRowsAreNotProtected() {
        givenTheCatalogue();

        assertThatCode(() -> execute("DELETE FROM pokemon WHERE id = 25")).doesNotThrowAnyException();
    }

    // --- item names ------------------------------------------------------------------------

    @Test
    @DisplayName("Two items cannot share a name, even under different ids")
    void itemNamesAreUniqueAcrossIds() {
        execute("INSERT INTO item (id, name) VALUES (723, 'roseli-berry')");

        assertThatThrownBy(() -> execute("INSERT INTO item (id, name) VALUES (2279, 'roseli-berry')"))
                .hasStackTraceContaining("item_name_key");
    }

    // --- one seed run at a time --------------------------------------------------------------

    @Test
    @DisplayName("A second running seed log is refused by the partial index")
    void onlyOneSeedRunCanBeInFlight() {
        execute("INSERT INTO seed_log (status) VALUES ('Running')");

        assertThatThrownBy(() -> execute("INSERT INTO seed_log (status) VALUES ('Running')"))
                .hasStackTraceContaining("uq_seed_log_single_running");
    }

    @Test
    @DisplayName("Finished runs are unaffected — any number may coexist")
    void finishedRunsAreNotConstrained() {
        execute("INSERT INTO seed_log (status) VALUES ('Completed')");
        execute("INSERT INTO seed_log (status) VALUES ('Completed')");
        execute("INSERT INTO seed_log (status) VALUES ('Failed')");
        execute("INSERT INTO seed_log (status) VALUES ('Running')");

        assertThat(count("SELECT COUNT(*) FROM seed_log")).isEqualTo(4L);
    }

    @Test
    @DisplayName("Closing the running row frees the slot for the next run")
    void closingARunFreesTheSlot() {
        execute("INSERT INTO seed_log (id, status) VALUES (1, 'Running')");
        execute("UPDATE seed_log SET status = 'Failed' WHERE id = 1");

        assertThatCode(() -> execute("INSERT INTO seed_log (id, status) VALUES (2, 'Running')"))
                .doesNotThrowAnyException();
    }

    // --- account tombstones --------------------------------------------------------------------

    @Test
    @DisplayName("A tombstoned account frees its username for someone else")
    void tombstonedUsernamesAreReusable() {
        execute("""
                INSERT INTO app_user (id, username, email, password_hash, deleted_at)
                VALUES (1, 'ash', 'ash@old.test', 'hash', NOW())
                """);

        assertThatCode(() -> execute("""
                INSERT INTO app_user (id, username, email, password_hash)
                VALUES (2, 'ash', 'ash@new.test', 'hash')
                """)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Two live accounts still cannot share a username")
    void liveUsernamesRemainUnique() {
        execute("""
                INSERT INTO app_user (id, username, email, password_hash)
                VALUES (1, 'ash', 'ash@one.test', 'hash')
                """);

        assertThatThrownBy(() -> execute("""
                INSERT INTO app_user (id, username, email, password_hash)
                VALUES (2, 'ash', 'ash@two.test', 'hash')
                """)).hasStackTraceContaining("uq_app_user_username_active");
    }

    // --- fixtures ------------------------------------------------------------------------------

    private void givenTheCatalogue() {
        execute("INSERT INTO type (id, name) VALUES (13, 'electric')");
        execute("INSERT INTO ability (id, name) VALUES (9, 'static')");
        execute("""
                INSERT INTO pokemon_species (id, name, genus, national_dex_number, generation)
                VALUES (25, 'pikachu', 'Mouse', 25, 1)
                """);
        execute("""
                INSERT INTO pokemon (id, name, primary_type_id, species_id,
                                     base_hp, base_atk, base_def, base_sp_atk, base_sp_def, base_speed)
                VALUES (25, 'pikachu', 13, 25, 35, 55, 40, 50, 50, 90)
                """);
    }

    private void givenATeamWithOnePokemon() {
        givenTheCatalogue();
        execute("""
                INSERT INTO app_user (id, username, email, password_hash)
                VALUES (1, 'ash', 'ash@test.local', 'hash')
                """);
        execute("INSERT INTO team (id, user_id, name) VALUES (1, 1, 'Kanto')");
        execute("""
                INSERT INTO team_pokemon (id, team_id, slot_position, pokemon_id, ability_id)
                VALUES (1, 1, 1, 25, 9)
                """);
    }

    private void execute(String sql) {
        em.createNativeQuery(sql).executeUpdate();
        em.flush();
    }

    private long count(String sql) {
        return ((Number) em.createNativeQuery(sql).getSingleResult()).longValue();
    }
}
