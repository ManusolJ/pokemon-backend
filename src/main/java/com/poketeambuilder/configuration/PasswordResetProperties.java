package com.poketeambuilder.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Password-reset link settings, bound from {@code app.password-reset}.
 *
 * @param tokenExpirationMinutes how long a reset token stays usable
 * @param baseUrl                SPA page the reset link points at; the token is appended as a query parameter
 */
@Validated
@ConfigurationProperties("app.password-reset")
public record PasswordResetProperties(
        @DefaultValue("30") @Positive int tokenExpirationMinutes,
        @NotBlank String baseUrl) {
}
