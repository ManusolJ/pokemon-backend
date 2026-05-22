package com.poketeambuilder.dtos.front.pokemon.species;

import com.poketeambuilder.dtos.front.type.single.TypeReadDto;

public record PokemonSpeciesSummaryDto(
    int id,
    String name,
    String genus,
    Integer nationalDexNumber,
    Integer order,
    Integer genderRate,
    TypeReadDto primaryType,
    TypeReadDto secondaryType,
    String spriteDefault
) {

}
