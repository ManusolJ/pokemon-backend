package com.poketeambuilder.dtos.front.nature;

import com.poketeambuilder.interfaces.FilterDtoInterface;

public class NatureFilterDto implements FilterDtoInterface{
    
    private Long id;

    private String name;

    private String nameExact;

    private String increasedStat;

    private String decreasedStat;

    @Override
    public boolean hasAnyCriteria() {
        return id != 0
                || (name != null && !name.isBlank())
                || (nameExact != null && !nameExact.isBlank())
                || (increasedStat != null && !increasedStat.isBlank())
                || (decreasedStat != null && !decreasedStat.isBlank());
    }
}
