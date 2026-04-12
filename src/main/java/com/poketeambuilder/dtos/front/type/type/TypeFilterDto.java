package com.poketeambuilder.dtos.front.type.type;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class TypeFilterDto implements FilterDtoInterface {

    private Integer id;

    private String name;

    private String nameExact;

    @Override
    public boolean hasAnyCriteria() {
        return id != null
                || name != null
                || nameExact != null;
    }
}
