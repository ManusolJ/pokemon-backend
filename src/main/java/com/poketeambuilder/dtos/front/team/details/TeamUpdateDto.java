package com.poketeambuilder.dtos.front.team.details;

import com.poketeambuilder.dtos.front.team.roster.TeamPokemonCreateDto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

/** Full-replacement payload for PUT /teams/{id}. */
@Getter
public class TeamUpdateDto {

    @NotBlank
    @Size(max = 50, message = "Team name must be at most 50 characters long")
    private String name;

    private Boolean isPublic;

    @Valid
    @Size(min = 1, max = 6, message = "A team must have between 1 and 6 Pokemon")
    private List<TeamPokemonCreateDto> pokemon;
}
