package com.poketeambuilder.dtos.pokeapi.evolution;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvolutionDetailApiDto(
    PokeApiResource item,
    PokeApiResource trigger,
    @JsonProperty("min_level") Integer minLevel,
    @JsonProperty("time_of_day") String timeOfDay,
    @JsonProperty("min_happiness") Integer minHappiness,
    @JsonProperty("held_item") PokeApiResource heldItem
) {}