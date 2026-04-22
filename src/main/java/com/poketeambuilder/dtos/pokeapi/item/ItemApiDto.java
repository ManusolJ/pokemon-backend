package com.poketeambuilder.dtos.pokeapi.item;

import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.common.ItemFlavorTextEntry;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemApiDto(
        Integer id,
        String name,
        ItemSprite sprites,
        PokeApiResource category,
        @JsonProperty("effect_entries") List<EffectEntry> effectEntries,
        @JsonProperty("flavor_text_entries") List<ItemFlavorTextEntry> flavorTextEntries
) {

}
