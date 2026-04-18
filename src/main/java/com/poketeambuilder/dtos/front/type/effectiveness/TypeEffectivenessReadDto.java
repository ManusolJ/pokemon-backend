package com.poketeambuilder.dtos.front.type.effectiveness;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;

public record TypeEffectivenessReadDto(TypeReadDto attackingType, TypeReadDto defendingType, double multiplier) {
    
}
