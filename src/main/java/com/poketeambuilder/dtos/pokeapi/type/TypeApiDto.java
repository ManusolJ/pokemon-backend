package com.poketeambuilder.dtos.pokeapi.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TypeApiDto(
        Integer id,
        String name,
        @JsonProperty("damage_relations") DamageRelations damageRelations
) {
}
