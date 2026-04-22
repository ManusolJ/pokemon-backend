package com.poketeambuilder.dtos.REST.common;

import com.poketeambuilder.interfaces.LocalizedEntry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FlavorTextEntry(
    PokeApiResource version,
    PokeApiResource language,
    @JsonProperty("flavor_text") String flavor_text
) implements LocalizedEntry{
    
}
