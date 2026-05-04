package com.poketeambuilder.infrastructure.validation.validators;

import com.poketeambuilder.dtos.auth.RegisterDto;
import com.poketeambuilder.infrastructure.validation.annotations.PasswordMatch;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordMatch, RegisterDto> {

    @Override
    public boolean isValid(RegisterDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        if (dto.getPassword() == null || dto.getConfirmPassword() == null) {
            return true;
        }

        boolean matches = dto.getPassword().equals(dto.getConfirmPassword());

        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                   .addPropertyNode("confirmPassword")
                   .addConstraintViolation();
        }

        return matches;
    }
}
