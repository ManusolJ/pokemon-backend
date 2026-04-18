package com.poketeambuilder.dtos.front.ability;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class AbilityFilterDto implements FilterDtoInterface {
    
    private Integer id;

    private String name;

    private String nameExact;
}
