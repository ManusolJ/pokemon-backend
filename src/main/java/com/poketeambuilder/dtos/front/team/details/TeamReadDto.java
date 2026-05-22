package com.poketeambuilder.dtos.front.team.details;

import com.poketeambuilder.dtos.front.user.UserSummaryDto;
import com.poketeambuilder.dtos.front.team.roster.TeamPokemonReadDto;

import java.util.List;
import java.time.Instant;

/**
 * Full team projection including the resolved owner and the six-slot roster.
 * {@link #likedByCurrentUser} is populated by the read path when an authenticated user is
 * known or {@code null} when the request is anonymous.
 */
public record TeamReadDto(
    long id,
    String name,
    String slug,
    boolean isPublic,
    Integer likeCount,
    Instant createdAt,
    Instant updatedAt,
    UserSummaryDto owner,
    List<TeamPokemonReadDto> pokemon,
    Boolean likedByCurrentUser
) {}
