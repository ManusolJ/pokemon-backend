package com.poketeambuilder.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Token signing and lifetime settings, bound from {@code app.jwt}.
 *
 * <p>{@code secret} has no default on purpose: an instance that starts with a fallback signing key
 * would issue tokens anyone holding this source could forge. {@code @NotBlank} turns a missing
 * {@code JWT_SECRET} into a startup failure that names the property, rather than a placeholder
 * resolution error further down.</p>
 *
 * @param secret                   base64-encoded HMAC key, at least 256 bits once decoded
 * @param accessTokenExpirationMs  lifetime of an access token
 * @param refreshTokenExpirationMs lifetime of a refresh token
 */
@Validated
@ConfigurationProperties("app.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @Positive long accessTokenExpirationMs,
        @Positive long refreshTokenExpirationMs) {
}
