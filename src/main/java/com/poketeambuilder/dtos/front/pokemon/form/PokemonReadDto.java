package com.poketeambuilder.dtos.front.pokemon.form;

import com.poketeambuilder.dtos.front.ability.AbilityEmbedDto;
import com.poketeambuilder.dtos.front.type.single.TypeReadDto;
import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesSummaryDto;

import java.util.List;

/**
 * Full Pokémon (form-level) projection — battle stats, types, sprites, abilities, and the
 * parent species.
 */
public record PokemonReadDto(
    int id,
    String name,
    Integer order,
    PokemonSpeciesSummaryDto species,
    Boolean isDefaultForm,
    TypeReadDto primaryType,
    TypeReadDto secondaryType,
    Integer baseHp,
    Integer baseAtk,
    Integer baseDef,
    Integer baseSpAtk,
    Integer baseSpDef,
    Integer baseSpeed,
    Double heightInMeters,
    Double weightInKilograms,
    List<AbilityEmbedDto> abilities,
    String spriteDefault,
    String spriteShiny,
    String artworkUrl,
    String artworkShiny
) {}
