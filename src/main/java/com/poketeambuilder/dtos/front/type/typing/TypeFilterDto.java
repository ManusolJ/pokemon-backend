package com.poketeambuilder.dtos.front.type.typing;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class TypeFilterDto implements FilterDtoInterface {

    private Integer id;

    private String name;

    private String nameExact;
}
