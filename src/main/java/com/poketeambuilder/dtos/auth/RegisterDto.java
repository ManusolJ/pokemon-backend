package com.poketeambuilder.dtos.auth;

import com.poketeambuilder.infrastructure.validation.annotations.PasswordMatch;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

/**
 * Payload for registration. Bounds mirror the database constraints.
 *
 * <p>The class-level {@link PasswordMatch} enforces that {@link #password} and
 * {@link #confirmPassword} are equal.</p>
 */
@Getter
@PasswordMatch
public class RegisterDto {

    @NotBlank
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters long")
    private String username;

    @Email
    @NotBlank
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters long")
    private String password;

    @NotBlank
    private String confirmPassword;
}
