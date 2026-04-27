package com.poketeambuilder.dtos.front.type.effectiveness;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class TypeEffectivenessFilterDto implements FilterDtoInterface {
    
    private BigDecimal multiplier;
    
    private Integer attackingTypeId;
    
    private Integer defendingTypeId;
}
