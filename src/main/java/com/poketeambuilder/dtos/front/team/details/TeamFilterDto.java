package com.poketeambuilder.dtos.front.team.details;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;
import lombok.Setter;

/**
 * Filter payload for team listings.
 */
@Getter
@Setter
public class TeamFilterDto implements FilterDtoInterface {

    private Long id;

    private Long userId;

    private String slug;

    private String name;

    private String nameExact;

    private Boolean isPublic;
}
