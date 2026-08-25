package com.poketeambuilder.dtos.front.user;

import java.time.Instant;

/**
 * Full user projection for the self-service and admin read paths.
 */
public record UserReadDto(long id, String username, String email, String role, boolean enabled, Instant createdAt, Instant deletedAt) {

}
