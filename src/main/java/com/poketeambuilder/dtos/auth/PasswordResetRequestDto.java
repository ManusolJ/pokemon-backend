package com.poketeambuilder.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

@Getter
public class PasswordResetRequestDto {
    
    @Email
    @NotBlank
    private String email;
}
