package com.poketeambuilder.dtos.front.nature;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class NatureFilterDto implements FilterDtoInterface{
    
    private Integer id;

    private String name;

    private String nameExact;

    private String increasedStat;

    private String decreasedStat;
}
