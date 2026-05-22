package com.poketeambuilder.dtos.front.type.single;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

/**
 * Filter payload for type listings.
 */
@Getter
public class TypeFilterDto implements FilterDtoInterface {

    private Integer id;

    private String name;

    private String nameExact;
}
