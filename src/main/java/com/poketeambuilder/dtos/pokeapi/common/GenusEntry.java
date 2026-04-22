package com.poketeambuilder.dtos.pokeapi.common;

import com.poketeambuilder.interfaces.LocalizedEntry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenusEntry(
    String genus,
    PokeApiResource language
) implements LocalizedEntry {

} 

