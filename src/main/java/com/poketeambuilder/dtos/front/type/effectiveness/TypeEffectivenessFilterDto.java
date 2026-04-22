package com.poketeambuilder.dtos.front.type.effectiveness;

import java.math.BigDecimal;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class TypeEffectivenessFilterDto implements FilterDtoInterface {
    
    private BigDecimal multiplier;
    
    private Integer attackingTypeId;
    
    private Integer defendingTypeId;
}
