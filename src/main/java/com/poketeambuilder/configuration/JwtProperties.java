package com.poketeambuilder.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import org.springframework.validation.annotation.Validated;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Validated
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    @NotBlank
    String secret,
    
    @Positive
    long accessTokenExpirationMs,

    @Positive
    long refreshTokenExpirationMs
) {}
