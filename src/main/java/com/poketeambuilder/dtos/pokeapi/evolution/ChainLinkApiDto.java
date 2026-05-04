package com.poketeambuilder.dtos.pokeapi.evolution;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChainLinkApiDto(
    PokeApiResource species,
    @JsonProperty("evolves_to") List<ChainLinkApiDto> evolvesTo,
    @JsonProperty("evolution_details") List<EvolutionDetailApiDto> evolutionDetails
) {}