package com.poketeambuilder.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poketeambuilder.dtos.auth.LoginDto;
import com.poketeambuilder.dtos.auth.PasswordResetDto;
import com.poketeambuilder.dtos.auth.PasswordResetRequestDto;
import com.poketeambuilder.dtos.auth.RegisterDto;
import com.poketeambuilder.dtos.auth.RefreshTokenDto;
import com.poketeambuilder.dtos.auth.TokenResponseDto;

import com.poketeambuilder.services.auth.AuthService;
import com.poketeambuilder.services.auth.PasswordResetService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponseDto> register(@Valid @RequestBody RegisterDto registerDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerDto));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(authService.login(loginDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
        authService.logout(refreshTokenDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto requestDto) {
        passwordResetService.requestReset(requestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetDto resetDto) {
        passwordResetService.resetPassword(resetDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
        return ResponseEntity.ok(authService.refresh(refreshTokenDto));
    }
}