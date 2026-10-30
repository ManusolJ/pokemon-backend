package com.poketeambuilder.dtos.auth;

import com.poketeambuilder.infrastructure.validation.annotations.MaxUtf8Bytes;

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
    @MaxUtf8Bytes(value = MaxUtf8Bytes.BCRYPT_MAX, message = "Password must be at most 72 bytes long; non-ASCII characters take more than one")
    private String newPassword;
}
