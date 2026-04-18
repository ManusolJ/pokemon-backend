package com.poketeambuilder.dtos.front.pokemon;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;

public record PokemonSummaryDto(long id, String name, Integer order, TypeReadDto primaryType, TypeReadDto secondaryType, String spriteDefault) {
    
}
