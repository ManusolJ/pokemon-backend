package com.poketeambuilder.dtos.auth;

import com.poketeambuilder.infrastructure.validation.annotations.MaxUtf8Bytes;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

/**
 * Payload for login. {@link #identifier} accepts either a username or an email.
 * {@link #password} is capped at bcrypt's 72-byte input limit, which the encoder enforces by
 * throwing; no stored password can be longer.
 */
@Getter
public class LoginDto {

    @NotBlank
    private String identifier;

    @NotBlank
    @MaxUtf8Bytes(MaxUtf8Bytes.BCRYPT_MAX)
    private String password;
}
