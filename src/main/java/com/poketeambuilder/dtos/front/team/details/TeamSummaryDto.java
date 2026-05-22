package com.poketeambuilder.dtos.front.team.details;

import java.util.List;
import java.time.Instant;

import com.poketeambuilder.dtos.front.user.UserSummaryDto;

/**
 * Compact team projection for listings. Carries only the six sprite URLs instead of the full
 * roster, keeping payloads small for browse / search endpoints.
 */
public record TeamSummaryDto(
    long id,
    String name,
    boolean isPublic,
    Integer likeCount,
    Instant createdAt,
    UserSummaryDto owner,
    List<String> pokemonSprites,
    Boolean likedByCurrentUser
) {}
