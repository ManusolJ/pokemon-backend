package com.poketeambuilder.dtos.front.pokemon;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class PokemonFilterDto implements FilterDtoInterface {

    private Integer id;

    private String name;

    private String nameExact;

    private Integer primaryTypeId;

    private Integer secondaryTypeId;
    
    private Integer minHeight;

    private Integer maxHeight;

    private Integer minWeight;

    private Integer maxWeight;
    
    private Boolean isDefaultForm;

    private Boolean isLegendary;

    private Boolean isMythical;

    private Boolean isBaby;
}