package com.poketeambuilder.dtos.front.pokemon.individual;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;
import com.poketeambuilder.dtos.front.ability.AbilityEmbedDto;
import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesSummaryDto;

import java.util.List;

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
    String artworkUrl
) {}