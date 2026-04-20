package com.poketeambuilder.dtos.front.team;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class TeamFilterDto implements FilterDtoInterface {

    private Long id;

    private Long userId;

    private String slug;

    private String name;

    private String nameExact;

    private Boolean isPublic;
}