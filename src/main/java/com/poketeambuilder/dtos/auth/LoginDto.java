package com.poketeambuilder.dtos.auth;

import lombok.Getter;

import jakarta.validation.constraints.NotBlank;

@Getter
public class LoginDto {

    @NotBlank
    private String identifier;

    @NotBlank
    private String password;
}
