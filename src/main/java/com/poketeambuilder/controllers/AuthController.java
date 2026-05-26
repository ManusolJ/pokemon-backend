package com.poketeambuilder.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poketeambuilder.dtos.auth.LoginDto;
import com.poketeambuilder.dtos.auth.RegisterDto;
import com.poketeambuilder.dtos.auth.TokenResponseDto;
import com.poketeambuilder.dtos.auth.PasswordResetConfirmDto;
import com.poketeambuilder.dtos.auth.PasswordResetRequestDto;
import com.poketeambuilder.dtos.auth.RefreshTokenRequestDto;

import com.poketeambuilder.services.auth.AuthService;
import com.poketeambuilder.services.auth.PasswordResetService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

/**
 * Unauthenticated authentication surface: register, login, logout, refresh, and the
 * password-reset request / confirm pair. Rate-limited per-IP by
 * {@link AuthRateLimitFilter}.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    /** Creates a new account and returns an access + refresh token pair. */
    @PostMapping("/register")
    public ResponseEntity<TokenResponseDto> register(@Valid @RequestBody RegisterDto registerDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerDto));
    }

    /** Exchanges credentials for an access + refresh token pair. */
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(authService.login(loginDto));
    }

    /** Revokes the supplied refresh token. The short-lived access token expires naturally. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDto refreshTokenDto) {
        authService.logout(refreshTokenDto);
        return ResponseEntity.noContent().build();
    }

    /** Issues a password-reset email when the address matches a known account. Silent otherwise. */
    @PostMapping("/password-reset-request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto requestDto) {
        passwordResetService.requestReset(requestDto);
        return ResponseEntity.noContent().build();
    }

    /** Consumes the reset token, sets the new password, and revokes every active session. */
    @PostMapping("/password-reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetConfirmDto resetDto) {
        passwordResetService.resetPassword(resetDto);
        return ResponseEntity.noContent().build();
    }

    /** Rotates a refresh token, returning a fresh access + refresh pair. */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto refreshTokenDto) {
        return ResponseEntity.ok(authService.refresh(refreshTokenDto));
    }
}