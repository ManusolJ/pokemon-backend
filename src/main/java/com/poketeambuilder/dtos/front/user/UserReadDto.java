package com.poketeambuilder.dtos.front.user;

import java.time.Instant;

public record UserReadDto(long id, String username, String email, String role, boolean enabled, Instant createdAt) {
    
}
