package com.poketeambuilder.utils.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.poketeambuilder.utils.validation.validators.ValidEvSpreadValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidEvSpreadValidator.class)
public @interface ValidEvSpread {

    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};

    String message() default "Total EVs cannot exceed 510";
}
