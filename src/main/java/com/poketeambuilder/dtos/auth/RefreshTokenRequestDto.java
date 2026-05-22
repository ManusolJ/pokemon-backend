package com.poketeambuilder.dtos.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Carries the raw refresh token. The server hashes and looks it up in {@code refresh_token}.
 */
public record RefreshTokenRequestDto(@NotBlank String refreshToken) {}
