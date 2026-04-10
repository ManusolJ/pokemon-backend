package com.poketeambuilder.dtos.front.move;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class MoveFilterDto implements FilterDtoInterface{
    
    private Long id;

    private String name;

    private String nameExact;

    private Long typeId;

    private String category;

    private Integer pp;

    private Integer power;

    private Integer accuracy;

    private Integer priority;

    @Override
    public boolean hasAnyCriteria() {
        return id != null
                || (name != null && !name.isBlank())
                || (nameExact != null && !nameExact.isBlank())
                || typeId != null
                || (category != null && !category.isBlank())
                || pp != null
                || power != null
                || accuracy != null
                || priority != null;
    }

}
