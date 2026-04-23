package com.poketeambuilder.dtos.pokeapi.ability;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokemonAbilityApiDto(
        Integer slot,
        PokeApiResource ability,
        @JsonProperty("is_hidden") Boolean isHidden
) {}
