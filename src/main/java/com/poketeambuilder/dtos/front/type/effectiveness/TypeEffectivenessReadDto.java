package com.poketeambuilder.dtos.front.type.effectiveness;

import com.poketeambuilder.dtos.front.type.single.TypeReadDto;

import java.math.BigDecimal;

public record TypeEffectivenessReadDto(TypeReadDto attackingType, TypeReadDto defendingType, BigDecimal multiplier) {
    
}
