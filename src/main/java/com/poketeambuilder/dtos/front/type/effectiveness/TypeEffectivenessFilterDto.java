package com.poketeambuilder.dtos.front.type.effectiveness;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class TypeEffectivenessFilterDto implements FilterDtoInterface {
    
    private Double multiplier;
    
    private Integer attackingTypeId;
    
    private Integer defendingTypeId;
}
