package com.poketeambuilder.dtos.front.team.pokemon;

import com.poketeambuilder.utils.validation.annotations.ValidEvSpread;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;

@Getter
@ValidEvSpread
public class TeamPokemonCreateDto {

    @NotNull
    private Integer pokemonId;

    @NotNull
    private Integer abilityId;

    @NotNull
    private Integer natureId;

    private Integer itemId;

    private Integer teraTypeId;

    @Size(max = 12)
    private String nickname;

    @Min(1) @Max(100)
    private Integer level;

    private String gender;

    private Boolean shiny;

    @Min(0) @Max(252) private Integer evHp;
    @Min(0) @Max(252) private Integer evAtk;
    @Min(0) @Max(252) private Integer evDef;
    @Min(0) @Max(252) private Integer evSpAtk;
    @Min(0) @Max(252) private Integer evSpDef;
    @Min(0) @Max(252) private Integer evSpeed;

    @Min(0) @Max(31) private Integer ivHp;
    @Min(0) @Max(31) private Integer ivAtk;
    @Min(0) @Max(31) private Integer ivDef;
    @Min(0) @Max(31) private Integer ivSpAtk;
    @Min(0) @Max(31) private Integer ivSpDef;
    @Min(0) @Max(31) private Integer ivSpeed;

    @NotNull
    @Size(min = 1, max = 4)
    private List<@NotNull Integer> moveIds;
}