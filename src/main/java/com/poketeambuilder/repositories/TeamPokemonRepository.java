package com.poketeambuilder.repositories;

import java.util.List;
import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import com.poketeambuilder.entities.Team;
import com.poketeambuilder.entities.TeamPokemon;

import com.poketeambuilder.repositories.projections.TeamSpriteProjection;

/**
 * CRUD + targeted reads for {@link TeamPokemon}. The bulk read joins every association
 * needed to render a team in one query, avoiding n+1 fetches in the team detail endpoints;
 * the lightweight projection skips that join when the only needes sprite URLs.
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
     * tera type. Used by the team-detail read path to avoid n+1.
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

    /**
     * Returns just the per-slot Pokémon sprite URL for the supplied teams. Used by the
     * team-summary listing where the rest of the roster join isn't needed.
     */
    @Query("""
            SELECT tp.team.id AS teamId,
                   tp.slot AS slot,
                   tp.pokemon.spriteDefault AS spriteDefault
              FROM TeamPokemon tp
             WHERE tp.team.id IN :teamIds
             ORDER BY tp.slot ASC
            """)
    List<TeamSpriteProjection> findSpritesByTeamIdIn(@Param("teamIds") Collection<Long> teamIds);
}
