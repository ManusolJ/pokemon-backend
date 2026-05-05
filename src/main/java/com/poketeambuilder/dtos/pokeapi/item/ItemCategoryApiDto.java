package com.poketeambuilder.dtos.pokeapi.item;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemCategoryApiDto(
    String name,
    List<PokeApiResource> items
) {}