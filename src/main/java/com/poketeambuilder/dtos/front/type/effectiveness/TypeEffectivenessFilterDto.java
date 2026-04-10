package com.poketeambuilder.dtos.front.type.effectiveness;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class TypeEffectivenessFilterDto implements FilterDtoInterface{
    
    private long id;

    private double multiplier;
    
    private long attackingTypeId;
    
    private long defendingTypeId;

    @Override
    public boolean hasAnyCriteria() {
        return id != 0
                || multiplier != 0
                || attackingTypeId != 0
                || defendingTypeId != 0;
    }
}
