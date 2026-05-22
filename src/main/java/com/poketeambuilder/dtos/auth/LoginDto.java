package com.poketeambuilder.dtos.auth;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

/**
 * Payload for login. {@link #identifier} accepts either a username or an email.
 */
@Getter
public class LoginDto {

    @NotBlank
    private String identifier;

    @NotBlank
    private String password;
}
