package com.poketeambuilder.utils.validation.validators;

import com.poketeambuilder.dtos.front.team.pokemon.TeamPokemonCreateDto;
import com.poketeambuilder.utils.validation.annotations.ValidEvSpread;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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

        if (total <= MAX_TOTAL_EVS) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
               .addPropertyNode("evHp")
               .addConstraintViolation();

        return false;
    }

    private int orZero(Integer value) {
        return value != null ? value : 0;
    }
}