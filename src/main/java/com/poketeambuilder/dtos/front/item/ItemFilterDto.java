package com.poketeambuilder.dtos.front.item;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class ItemFilterDto implements FilterDtoInterface {
    
    private Integer id;

    private String name;

    private String nameExact;    
}
