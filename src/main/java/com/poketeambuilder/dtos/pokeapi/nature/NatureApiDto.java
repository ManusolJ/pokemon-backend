package com.poketeambuilder.dtos.pokeapi.nature;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NatureApiDto(
        Integer id,
        String name,
        @JsonProperty("increased_stat") PokeApiResource increasedStat,
        @JsonProperty("decreased_stat") PokeApiResource decreasedStat
) {}