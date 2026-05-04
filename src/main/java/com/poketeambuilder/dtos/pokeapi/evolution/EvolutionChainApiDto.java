package com.poketeambuilder.dtos.pokeapi.evolution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvolutionChainApiDto(
    Integer id,
    ChainLinkApiDto chain
) {}