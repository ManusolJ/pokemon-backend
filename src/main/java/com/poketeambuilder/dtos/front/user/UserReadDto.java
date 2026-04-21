package com.poketeambuilder.dtos.front.user;

public record UserReadDto(long id, String username, String email, String role, boolean enabled) {
    
}
