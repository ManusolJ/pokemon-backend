package com.poketeambuilder.dtos.pokeapi.type;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DamageRelations(
        @JsonProperty("no_damage_to") List<PokeApiResource> noDamageTo,
        @JsonProperty("half_damage_to") List<PokeApiResource> halfDamageTo,
        @JsonProperty("no_damage_from") List<PokeApiResource> noDamageFrom,
        @JsonProperty("half_damage_from") List<PokeApiResource> halfDamageFrom,
        @JsonProperty("double_damage_to") List<PokeApiResource> doubleDamageTo,
        @JsonProperty("double_damage_from") List<PokeApiResource> doubleDamageFrom
) {}
    