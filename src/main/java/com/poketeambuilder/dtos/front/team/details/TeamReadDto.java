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
) {

    /**
     * Copy with the roster and per-viewer like flag filled in. The mapper leaves both unset;
     * the query layer loads them in batch. Kept here so a new component can't be forgotten at
     * a far-away call site that rebuilds the record by position.
     */
    public TeamReadDto withRosterAndLike(List<TeamPokemonReadDto> roster, boolean liked) {
        return new TeamReadDto(id, name, slug, isPublic, likeCount, createdAt, updatedAt, owner, roster, liked);
    }
}
