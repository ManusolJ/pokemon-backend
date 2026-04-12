package com.poketeambuilder.dtos.front.nature;

import com.poketeambuilder.interfaces.FilterDtoInterface;

public class NatureFilterDto implements FilterDtoInterface{
    
    private Integer id;

    private String name;

    private String nameExact;

    private String increasedStat;

    private String decreasedStat;

    @Override
    public boolean hasAnyCriteria() {
        return id != null
                || name != null
                || nameExact != null
                || increasedStat != null
                || decreasedStat != null;
    }
}
