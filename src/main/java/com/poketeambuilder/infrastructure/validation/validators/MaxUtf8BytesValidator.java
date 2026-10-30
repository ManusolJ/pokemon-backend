package com.poketeambuilder.infrastructure.validation.validators;

import java.nio.charset.StandardCharsets;

import com.poketeambuilder.infrastructure.validation.annotations.MaxUtf8Bytes;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Backs {@link MaxUtf8Bytes}: compares the UTF-8 encoded length against the configured cap. */
public class MaxUtf8BytesValidator implements ConstraintValidator<MaxUtf8Bytes, String> {

    private int maxBytes;

    @Override
    public void initialize(MaxUtf8Bytes annotation) {
        this.maxBytes = annotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.getBytes(StandardCharsets.UTF_8).length <= maxBytes;
    }
}
