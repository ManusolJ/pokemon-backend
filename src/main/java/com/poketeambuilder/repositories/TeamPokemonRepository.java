package com.poketeambuilder.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.Team;
import com.poketeambuilder.entities.TeamPokemon;

/**
 * CRUD + targeted reads for {@link TeamPokemon}. The bulk read joins every association
 * needed to render a team in one query, avoiding n+1 fetches in the team listing endpoints.
 */
public interface TeamPokemonRepository extends BaseRepository<TeamPokemon, Long> {

    /**
     * Bulk-deletes every {@link TeamPokemon} belonging to the given team.
     */
    @Modifying
    @Query("DELETE FROM TeamPokemon tp WHERE tp.team = :team")
    void deleteByTeam(@Param("team") Team team);

    /**
     * Loads every team-pokemon row for the supplied teams together with the join targets
     * needed to render them: base Pokémon (with both types), ability, nature, held item, and
     * tera type. Single SQL statement — used by the team-listing read path to avoid n+1.
     *
     * @param teamIds team ids to load (typically the current page of a listing)
     * @return rows ordered by {@link TeamPokemon#getSlot()} ascending
     */
    @Query("""
            SELECT tp FROM TeamPokemon tp
            JOIN FETCH tp.pokemon p
            JOIN FETCH p.primaryType
            LEFT JOIN FETCH p.secondaryType
            JOIN FETCH tp.ability
            LEFT JOIN FETCH tp.nature
            LEFT JOIN FETCH tp.heldItem
            LEFT JOIN FETCH tp.teraType
            WHERE tp.team.id IN :teamIds
            ORDER BY tp.slot ASC
            """)
    List<TeamPokemon> findByTeamIdInWithDetails(@Param("teamIds") List<Long> teamIds);
}
