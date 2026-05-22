package com.poketeambuilder.dtos.front.team.details;

import com.poketeambuilder.dtos.front.team.roster.TeamPokemonCreateDto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;

/** Payload for team creation. A team must have between 1 and 6 Pokémon and a non-blank name. */
@Getter
public class TeamCreateDto {

    @NotBlank
    @Size(max = 50, message = "Team name must be at most 50 characters long")
    String name;

    Boolean isPublic;

    @Valid
    @NotNull
    @Size(min = 1, max = 6, message = "A team must have between 1 and 6 Pokemon")
    List<TeamPokemonCreateDto> pokemon;
}
