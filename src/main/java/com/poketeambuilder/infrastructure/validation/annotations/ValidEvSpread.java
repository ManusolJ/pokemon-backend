package com.poketeambuilder.infrastructure.validation.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import com.poketeambuilder.infrastructure.validation.validators.ValidEvSpreadValidator;

import jakarta.validation.Payload;
import jakarta.validation.Constraint;

/**
 * Class-level constraint enforcing the EV-total cap (≤ 510) on a team-pokemon create payload.
 * Per-stat ranges are enforced by field-level {@code @Min}/{@code @Max}. Applied via
 * {@link ValidEvSpreadValidator}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidEvSpreadValidator.class)
public @interface ValidEvSpread {

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String message() default "Total EVs cannot exceed 510";
}
