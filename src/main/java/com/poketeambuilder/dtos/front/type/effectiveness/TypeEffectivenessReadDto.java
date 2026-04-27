package com.poketeambuilder.dtos.front.type.effectiveness;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;

import java.math.BigDecimal;

public record TypeEffectivenessReadDto(TypeReadDto attackingType, TypeReadDto defendingType, BigDecimal multiplier) {
    
}
