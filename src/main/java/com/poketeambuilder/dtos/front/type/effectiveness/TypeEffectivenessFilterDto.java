package com.poketeambuilder.dtos.front.type.effectiveness;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class TypeEffectivenessFilterDto implements FilterDtoInterface{
    
    private Integer id;

    private Double multiplier;
    
    private Integer attackingTypeId;
    
    private Integer defendingTypeId;

    @Override
    public boolean hasAnyCriteria() {
        return id != null
                || multiplier != null
                || attackingTypeId != null
                || defendingTypeId != null;
    }
}
