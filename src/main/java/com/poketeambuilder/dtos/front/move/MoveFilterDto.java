package com.poketeambuilder.dtos.front.move;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import jakarta.validation.constraints.Pattern;

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

    /**
     * Matched case-insensitively against {@link com.poketeambuilder.utils.enums.MoveCategory}.
     */
    @Pattern(regexp = "physical|special|status", flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "Category must be one of physical, special, status")
    private String category;

    private Integer priority;

    private Integer minPower;

    private Integer maxPower;

    private Integer minAccuracy;

    private Integer maxAccuracy;
}
