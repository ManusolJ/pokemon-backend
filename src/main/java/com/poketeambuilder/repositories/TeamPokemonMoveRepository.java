package com.poketeambuilder.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.Team;
import com.poketeambuilder.entities.TeamPokemonMove;
import com.poketeambuilder.entities.compositeIDs.TeamPokemonMoveId;

import java.util.List;

public interface TeamPokemonMoveRepository extends BaseRepository<TeamPokemonMove, TeamPokemonMoveId> {

    @Modifying
    @Query("DELETE FROM TeamPokemonMove tpm WHERE tpm.teamPokemon.team = :team")
    void deleteByTeamPokemonTeam(@Param("team") Team team);

    @Query("""
            SELECT tpm FROM TeamPokemonMove tpm
            JOIN FETCH tpm.move m
            JOIN FETCH m.type
            WHERE tpm.id.teamPokemonId IN :teamPokemonIds
            ORDER BY tpm.id.slotPosition ASC
            """)
    List<TeamPokemonMove> findByTeamPokemonIdInWithMove(@Param("teamPokemonIds") List<Long> teamPokemonIds);
}