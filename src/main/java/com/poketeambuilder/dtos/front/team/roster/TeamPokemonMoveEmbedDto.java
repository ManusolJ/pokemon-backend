package com.poketeambuilder.dtos.front.team.roster;

import com.poketeambuilder.dtos.front.move.MoveSummaryDto;

/** One of the (up to four) moves chosen for a team-pokemon slot. */
public record TeamPokemonMoveEmbedDto(MoveSummaryDto move, Integer slotPosition) {}
