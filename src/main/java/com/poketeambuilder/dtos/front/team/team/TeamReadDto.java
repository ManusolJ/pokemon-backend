package com.poketeambuilder.dtos.front.team.team;

import com.poketeambuilder.dtos.front.user.UserSummaryDto;
import com.poketeambuilder.dtos.front.team.pokemon.TeamPokemonReadDto;

import java.util.List;
import java.time.Instant;

public record TeamReadDto(
    long id,
    String name,
    String slug,
    boolean isPublic,
    Integer likeCount,
    Instant createdAt,
    Instant updatedAt,
    UserSummaryDto owner,
    List<TeamPokemonReadDto> pokemon
) {
    
}
