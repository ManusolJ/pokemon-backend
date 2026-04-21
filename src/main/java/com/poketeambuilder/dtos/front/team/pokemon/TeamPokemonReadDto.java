package com.poketeambuilder.dtos.front.team.pokemon;

import com.poketeambuilder.dtos.front.item.ItemSummaryDto;
import com.poketeambuilder.dtos.front.nature.NatureReadDto;
import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;
import com.poketeambuilder.dtos.front.ability.AbilitySummaryDto;
import com.poketeambuilder.dtos.front.pokemon.individual.PokemonSummaryDto;

import java.util.List;

public record TeamPokemonReadDto(
    long id,
    Integer slot,
    String nickname,
    Integer level,
    String gender,
    Boolean shiny,
    PokemonSummaryDto pokemon,
    AbilitySummaryDto ability,
    NatureReadDto nature,
    ItemSummaryDto heldItem,
    TypeReadDto teraType,
    Integer evHp,
    Integer evAtk,
    Integer evDef,
    Integer evSpAtk,
    Integer evSpDef,
    Integer evSpeed,
    Integer ivHp,
    Integer ivAtk,
    Integer ivDef,
    Integer ivSpAtk,
    Integer ivSpDef,
    Integer ivSpeed,
    List<TeamPokemonMoveEmbedDto> moves
) {}
