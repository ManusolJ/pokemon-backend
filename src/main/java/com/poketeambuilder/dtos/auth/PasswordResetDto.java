package com.poketeambuilder.dtos.auth;

import lombok.Getter;

@Getter
public class PasswordResetDto {
    
    private String token;

    private String newPassword;
}
