package com.poketeambuilder.dtos.front.pokemon.form;

import com.poketeambuilder.dtos.front.type.single.TypeReadDto;

/**
 * Lightweight Pokémon projection for listings and embeds — id, name, both types, and a sprite.
 * The {@code order} field preserves the upstream PokeAPI naming (the entity calls it {@code sortOrder}).
 */
public record PokemonSummaryDto(
    int id,
    String name,
    Integer order,
    TypeReadDto primaryType,
    TypeReadDto secondaryType,
    String spriteDefault,
    String spriteShiny
) {}
