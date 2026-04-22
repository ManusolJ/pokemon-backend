package com.poketeambuilder.dtos.pokeapi.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TypeApiDto(
        Integer id,
        String name,
        @JsonProperty("damage_relations") DamageRelations damageRelations
) {
}
