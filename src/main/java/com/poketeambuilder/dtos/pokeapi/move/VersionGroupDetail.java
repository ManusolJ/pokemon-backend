package com.poketeambuilder.dtos.pokeapi.move;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VersionGroupDetail(
        @JsonProperty("order") Integer order,
        @JsonProperty("level_learned_at") Integer levelLearnedAt,
        @JsonProperty("version_group") PokeApiResource versionGroup,
        @JsonProperty("move_learn_method") PokeApiResource moveLearnMethod
) {}
