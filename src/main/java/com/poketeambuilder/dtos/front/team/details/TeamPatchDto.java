package com.poketeambuilder.dtos.front.team.details;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * Partial-update payload for PATCH /teams/{id}. Every field is optional and only the ones set on
 * the payload are applied. Updating the team's roster requires a full {@link TeamUpdateDto}.
 */
@Getter
@Setter
public class TeamPatchDto {

    @Size(max = 50, message = "Team name must be at most 50 characters long")
    private String name;

    private Boolean isPublic;
}
