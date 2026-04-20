package com.poketeambuilder.dtos.front.team.team;

import java.util.List;

import com.poketeambuilder.dtos.front.user.UserSummaryDto;

public record TeamSummary(
    long id,
    String name,
    Integer likeCount,
    UserSummaryDto owner,
    List<String> pokemonSprites
) {
    
}
