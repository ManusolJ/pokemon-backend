package com.poketeambuilder.dtos.pokeapi.ability;

import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.AbilityFlavorTextEntry;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AbilityApiDto(
        Integer id,
        String name,
        @JsonProperty("is_main_series") Boolean isMainSeries,
        @JsonProperty("effect_entries") List<EffectEntry> effectEntries,
        @JsonProperty("flavor_text_entries") List<AbilityFlavorTextEntry> flavorTextEntries
) {}
