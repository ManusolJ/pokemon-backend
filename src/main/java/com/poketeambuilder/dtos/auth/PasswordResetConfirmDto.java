package com.poketeambuilder.dtos.auth;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

/**
 * Payload for the password-reset confirmation step: the one-time {@link #token} the user
 * received by e-mail plus the new password.
 */
@Getter
public class PasswordResetConfirmDto {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters long")
    private String newPassword;
}
