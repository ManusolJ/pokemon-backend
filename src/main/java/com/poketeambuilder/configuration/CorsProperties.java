package com.poketeambuilder.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Cross-origin settings, bound from {@code app.cors}.
 *
 * @param allowedOrigin scheme, host and port of the SPA, with no trailing slash
 */
@Validated
@ConfigurationProperties("app.cors")
public record CorsProperties(@NotBlank String allowedOrigin) {
}
