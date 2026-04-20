package com.poketeambuilder.dtos.front.team.team;

import java.util.List;

import com.poketeambuilder.dtos.front.team.pokemon.TeamPokemonCreateDto;

import lombok.Getter;

@Getter
public class TeamCreateDto {
    
    String name;

    Boolean isPublic;

    List<TeamPokemonCreateDto> pokemon;
}
