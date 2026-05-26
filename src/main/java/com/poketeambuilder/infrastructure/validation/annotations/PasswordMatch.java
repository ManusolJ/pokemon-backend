package com.poketeambuilder.infrastructure.validation.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import com.poketeambuilder.infrastructure.validation.validators.PasswordsMatchValidator;

import jakarta.validation.Payload;
import jakarta.validation.Constraint;

/**
 * Class-level constraint enforcing that {@code password} and {@code confirmPassword} fields
 * of a registration payload match. Applied via {@link PasswordsMatchValidator}.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordsMatchValidator.class)
public @interface PasswordMatch {

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String message() default "Passwords do not match";
}
