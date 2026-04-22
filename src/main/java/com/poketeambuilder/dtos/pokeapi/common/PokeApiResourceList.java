package com.poketeambuilder.dtos.pokeapi.common;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokeApiResourceList(
    int count,
    String next,
    String previous,
    List<PokeApiResource> results
) {
    
    public boolean hasNext() {
        return next != null && !next.isBlank();
    }

    public boolean isEmpty() {
        return results == null || results.isEmpty();
    }
}
