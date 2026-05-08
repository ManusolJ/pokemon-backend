package com.poketeambuilder.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.Team;
import com.poketeambuilder.entities.TeamPokemonMove;
import com.poketeambuilder.entities.compositeIDs.TeamPokemonMoveId;

public interface TeamPokemonMoveRepository extends BaseRepository<TeamPokemonMove, TeamPokemonMoveId> {
    
    @Modifying
    @Query("DELETE FROM TeamPokemonMove tpm WHERE tpm.teamPokemon = :team")
    void deleteByTeamPokemonTeam(@Param("team") Team team);
}
