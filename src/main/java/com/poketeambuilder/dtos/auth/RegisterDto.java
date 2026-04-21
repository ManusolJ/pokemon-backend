package com.poketeambuilder.dtos.auth;

import com.poketeambuilder.utils.validation.annotations.PasswordMatch;

import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

@Getter
@PasswordMatch
public class RegisterDto {

    @NotBlank
    @Size(min = 3, message = "Username must be at least 3 characters long")
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank
    private String confirmPassword;
}
