package com.poketeambuilder.dtos.pokeapi.pokemon;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.move.PokemonMoveApiDto;
import com.poketeambuilder.dtos.pokeapi.type.PokemonTypeApiDto;
import com.poketeambuilder.dtos.pokeapi.ability.PokemonAbilityApiDto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokemonApiDto(
        Integer id,
        String name,
        Integer order,
        Integer height,
        Integer weight,
        PokemonSprites sprites,
        PokeApiResource species,
        List<PokemonTypeApiDto> types,
        List<PokemonStatApiDto> stats,
        List<PokemonMoveApiDto> moves,
        List<PokemonAbilityApiDto> abilities,
        @JsonProperty("is_default") Boolean isDefault
) {

}