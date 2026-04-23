package com.poketeambuilder.dtos.pokeapi.species;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokedexNumber(
    PokeApiResource pokedex,
    @JsonProperty("entry_number") Integer entryNumber
) {}
