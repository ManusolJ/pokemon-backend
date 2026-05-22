package com.poketeambuilder.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.Team;
import com.poketeambuilder.entities.TeamPokemonMove;
import com.poketeambuilder.entities.compositeIDs.TeamPokemonMoveId;

/**
 * CRUD + targeted reads for {@link TeamPokemonMove}. Pairs with {@link TeamPokemonRepository}
 * so the team-listing read path fetches moves in a single query.
 */
public interface TeamPokemonMoveRepository extends BaseRepository<TeamPokemonMove, TeamPokemonMoveId> {

    /**
     * Bulk-deletes every move row belonging to any {@link TeamPokemon} of the given team.
     */
    @Modifying
    @Query("DELETE FROM TeamPokemonMove tpm WHERE tpm.teamPokemon.team = :team")
    void deleteByTeamPokemonTeam(@Param("team") Team team);

    /**
     * Loads every move row attached to the supplied team-pokemon ids, with the move and its
     * type already fetched. Single SQL statement; used by the team-listing read path.
     *
     * @param teamPokemonIds team-pokemon ids to load moves for
     * @return rows ordered by {@code slot_position} ascending
     */
    @Query("""
            SELECT tpm FROM TeamPokemonMove tpm
            JOIN FETCH tpm.move m
            JOIN FETCH m.type
            WHERE tpm.id.teamPokemonId IN :teamPokemonIds
            ORDER BY tpm.id.slotPosition ASC
            """)
    List<TeamPokemonMove> findByTeamPokemonIdInWithMove(@Param("teamPokemonIds") List<Long> teamPokemonIds);
}
