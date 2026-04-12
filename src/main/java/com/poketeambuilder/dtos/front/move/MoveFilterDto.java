package com.poketeambuilder.dtos.front.move;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class MoveFilterDto implements FilterDtoInterface{
    
    private Integer id;

    private String name;

    private String nameExact;

    private Long typeId;

    private String category;

    private Integer priority;

    private Integer minPower;

    private Integer maxPower;

    private Integer minAccuracy;

    private Integer maxAccuracy;

    @Override
    public boolean hasAnyCriteria() {
        return id != null
        || typeId != null
        || priority != null
        || minPower != null
        || maxPower != null
        || minAccuracy != null
        || maxAccuracy != null
        || name != null
        || category != null
        || nameExact != null;
    }

}
