package com.poketeambuilder.dtos.front.pokemon.individual;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;

import java.util.List;

import com.poketeambuilder.dtos.front.ability.AbilityEmbedDto;
import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesSummaryDto;

public record PokemonReadDto(
    long id,
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
    Double height,
    Double weight,
    List<AbilityEmbedDto> abilities,
    String spriteDefault,
    String spriteShiny,
    String artworkUrl
) {}