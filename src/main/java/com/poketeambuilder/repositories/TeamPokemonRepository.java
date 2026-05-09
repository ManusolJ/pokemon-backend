package com.poketeambuilder.repositories;

import com.poketeambuilder.entities.Team;
import com.poketeambuilder.entities.TeamPokemon;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamPokemonRepository extends BaseRepository<TeamPokemon, Long> {

    void deleteByTeam(Team team);

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