package com.poketeambuilder.dtos.pokeapi.pokemon;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokemonStatApiDto(
    Integer effort,
    PokeApiResource stat,
    @JsonProperty("base_stat") Integer baseStat 
) {}
