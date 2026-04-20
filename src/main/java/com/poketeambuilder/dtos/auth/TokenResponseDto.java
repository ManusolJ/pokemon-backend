package com.poketeambuilder.dtos.auth;

public record TokenResponseDto(String accessToken, String refreshToken, Integer expiresIn) {
    
}
