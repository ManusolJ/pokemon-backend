package com.poketeambuilder.dtos.front.type.effectiveness;

import java.math.BigDecimal;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;

public record TypeEffectivenessReadDto(TypeReadDto attackingType, TypeReadDto defendingType, BigDecimal multiplier) {
    
}
