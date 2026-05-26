package com.poketeambuilder.infrastructure.validation.validators;

import com.poketeambuilder.dtos.front.team.roster.TeamPokemonCreateDto;

import com.poketeambuilder.infrastructure.validation.annotations.ValidEvSpread;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Enforces the cross-field rule that the six EV stats sum to at most 510 — the canonical
 * Pokémon battle cap. Individual stat ranges (0–252 per stat) are enforced by per-field
 * {@code @Min}/{@code @Max} on the DTO; this validator only handles the cross-field total.
 * The violation is reported at the bean level so the front-end can render it as a
 * non-field-specific error.
 */
public class ValidEvSpreadValidator implements ConstraintValidator<ValidEvSpread, TeamPokemonCreateDto> {

    private static final int MAX_TOTAL_EVS = 510;

    @Override
    public boolean isValid(TeamPokemonCreateDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        int total = orZero(dto.getEvHp())
                  + orZero(dto.getEvAtk())
                  + orZero(dto.getEvDef())
                  + orZero(dto.getEvSpAtk())
                  + orZero(dto.getEvSpDef())
                  + orZero(dto.getEvSpeed());

        return total <= MAX_TOTAL_EVS;
    }

    private int orZero(Integer value) {
        return value != null ? value : 0;
    }
}
