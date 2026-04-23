package com.poketeambuilder.dtos.pokeapi.type;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokemonTypeApiDto(
        Integer slot,
        PokeApiResource type
) {}
