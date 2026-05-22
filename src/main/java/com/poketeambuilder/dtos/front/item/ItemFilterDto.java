package com.poketeambuilder.dtos.front.item;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

/**
 * Filter payload for item listings. {@link #name} performs a LIKE/contains match;
 * {@link #nameExact} forces exact comparison.
 */
@Getter
public class ItemFilterDto implements FilterDtoInterface {

    private Integer id;

    private String name;

    private String nameExact;
}
