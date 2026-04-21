package com.poketeambuilder.dtos.auth;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

@Getter
public class PasswordResetDto {
    
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
}
