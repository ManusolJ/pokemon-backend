package com.poketeambuilder.dtos.REST.common;

import com.poketeambuilder.interfaces.LocalizedEntry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NameEntry(
    String name,
    PokeApiResource language
) implements LocalizedEntry {
    
}
