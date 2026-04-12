package com.poketeambuilder.dtos.front.pokemon;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class PokemonFilterDto implements FilterDtoInterface {

    private Integer id;

    private String name;

    private String nameExact;

    private Integer nationalDexNumber;

    private Integer primaryTypeId;

    private Integer secondaryTypeId;
    
    private Integer generation;

    private Integer minHeight;

    private Integer maxHeight;

    private Integer minWeight;

    private Integer maxWeight;

    private Integer abilityId;
    
    private Boolean isDefaultForm;

    private Boolean isLegendary;

    private Boolean isMythical;

    private Boolean isBaby;

    @Override
    public boolean hasAnyCriteria() {
        return id != null
                || name != null
                || nameExact != null
                || minHeight != null
                || maxHeight != null
                || minWeight != null
                || maxWeight != null
                || abilityId != null
                || generation != null
                || isBaby != null
                || isMythical != null
                || isLegendary != null
                || isDefaultForm != null
                || primaryTypeId != null
                || secondaryTypeId != null
                || nationalDexNumber != null;
    }
}