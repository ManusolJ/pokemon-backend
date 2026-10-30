package com.poketeambuilder.infrastructure.validation.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import com.poketeambuilder.infrastructure.validation.validators.MaxUtf8BytesValidator;

import jakarta.validation.Payload;
import jakarta.validation.Constraint;

/**
 * Caps a string's length in UTF-8 <em>bytes</em> rather than characters. Exists for bcrypt,
 * whose 72-byte input limit a character count can't express: 40 accented or emoji characters
 * pass {@code @Size(max = 72)} and still overflow it, and the encoder refuses such input with an
 * {@link IllegalArgumentException} that would otherwise surface as a 500. {@code null} is valid.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxUtf8BytesValidator.class)
public @interface MaxUtf8Bytes {

    /** bcrypt's input limit. */
    int BCRYPT_MAX = 72;

    int value();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String message() default "Must be at most {value} bytes long when UTF-8 encoded";
}
