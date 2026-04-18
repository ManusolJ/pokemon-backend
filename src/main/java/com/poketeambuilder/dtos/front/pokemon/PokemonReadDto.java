package com.poketeambuilder.dtos.front.pokemon;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;
import com.poketeambuilder.dtos.front.species.PokemonSpeciesSummaryDto;

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
    String spriteDefault,
    String spriteShiny,
    String artworkUrl
) {}