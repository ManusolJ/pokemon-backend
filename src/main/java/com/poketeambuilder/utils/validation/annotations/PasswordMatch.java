package com.poketeambuilder.utils.validation.annotations;

import com.poketeambuilder.utils.validation.validators.PasswordsMatchValidator;

import jakarta.validation.Payload;
import jakarta.validation.Constraint;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordsMatchValidator.class)
public @interface PasswordMatch {

    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    String message() default "Passwords do not match";
}
