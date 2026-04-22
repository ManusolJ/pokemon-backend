package com.poketeambuilder.dtos.front.pokemon.individual;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;

public record PokemonSummaryDto(int id, String name, Integer order, TypeReadDto primaryType, TypeReadDto secondaryType, String spriteDefault) {
    
}
