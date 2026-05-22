package com.poketeambuilder.dtos.front.user;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

/**
 * Self-service password change. {@link #currentPassword} has no length bounds — a user's
 * existing password may pre-date a stricter rule, so we only require it to be present.
 * {@link #newPassword} is capped at 72 chars to avoid bcrypt's silent truncation surprise.
 */
@Getter
public class PasswordChangeDto {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters long")
    private String newPassword;
}
