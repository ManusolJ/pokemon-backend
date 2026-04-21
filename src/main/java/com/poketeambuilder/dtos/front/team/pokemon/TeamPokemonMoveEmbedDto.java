package com.poketeambuilder.dtos.front.team.pokemon;

import com.poketeambuilder.dtos.front.move.MoveSummaryDto;

public record TeamPokemonMoveEmbedDto(
    MoveSummaryDto move,
    Integer slotPosition
) {}