package com.poketeambuilder.infrastructure.validation.validators;

import com.poketeambuilder.dtos.auth.RegisterDto;

import com.poketeambuilder.infrastructure.validation.annotations.PasswordMatch;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Cross-field check that {@link RegisterDto#getPassword()} and
 * {@link RegisterDto#getConfirmPassword()} are identical.
 */
public class PasswordsMatchValidator implements ConstraintValidator<PasswordMatch, RegisterDto> {

    private static final String CONFIRM_PASSWORD_FIELD = "confirmPassword";

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
                   .addPropertyNode(CONFIRM_PASSWORD_FIELD)
                   .addConstraintViolation();
        }

        return matches;
    }
}
