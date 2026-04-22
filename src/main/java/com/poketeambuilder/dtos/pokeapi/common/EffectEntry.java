package com.poketeambuilder.dtos.pokeapi.common;

import com.poketeambuilder.interfaces.LocalizedEntry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EffectEntry(
    String effect,
    PokeApiResource language,
    @JsonProperty("short_effect") String shortEffect
) implements LocalizedEntry {
    
}
