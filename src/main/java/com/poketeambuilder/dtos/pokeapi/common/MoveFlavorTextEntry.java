package com.poketeambuilder.dtos.pokeapi.common;

import com.poketeambuilder.interfaces.LocalizedEntry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MoveFlavorTextEntry(
    PokeApiResource language,
    @JsonProperty("flavor_text") String flavorText,
    @JsonProperty("version_group") PokeApiResource versionGroup
) implements LocalizedEntry {}