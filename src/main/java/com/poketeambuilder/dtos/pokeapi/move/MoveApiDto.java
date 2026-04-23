package com.poketeambuilder.dtos.pokeapi.move;

import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MoveApiDto(
        Integer id,
        String name,
        Integer pp,
        Integer power,
        Integer accuracy,
        Integer priority,
        @JsonProperty("effect_chance") Integer effectChance,
        PokeApiResource type,
        @JsonProperty("damage_class") PokeApiResource damageClass,
        @JsonProperty("effect_entries") List<EffectEntry> effectEntries
) {}
