package com.poketeambuilder.dtos.pokeapi.type;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TypeApiDto(
        Integer id,
        String name,
        @JsonProperty("damage_relations") DamageRelations damageRelations
) {
}
