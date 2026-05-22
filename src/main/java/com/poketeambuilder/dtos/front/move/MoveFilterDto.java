package com.poketeambuilder.dtos.front.move;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

/**
 * Filter payload for move listings.
 */
@Getter
public class MoveFilterDto implements FilterDtoInterface {

    private Integer id;

    private Integer pokemonId;

    private String name;

    private String nameExact;

    private Integer typeId;

    private String category;

    private Integer priority;

    private Integer minPower;

    private Integer maxPower;

    private Integer minAccuracy;

    private Integer maxAccuracy;
}
