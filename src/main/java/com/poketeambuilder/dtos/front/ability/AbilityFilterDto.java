package com.poketeambuilder.dtos.front.ability;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class AbilityFilterDto implements FilterDtoInterface {
    
    private Long id;

    private String name;

    private String nameExact;

    @Override
    public boolean hasAnyCriteria() {
        return id != 0
                || (name != null && !name.isBlank())
                || (nameExact != null && !nameExact.isBlank());
    }
}
