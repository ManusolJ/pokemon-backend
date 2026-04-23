package com.poketeambuilder.dtos.pokeapi.move;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokemonMoveApiDto(
        PokeApiResource move,
        @JsonProperty("version_group_details") List<VersionGroupDetail> versionGroupDetails
) {

}
