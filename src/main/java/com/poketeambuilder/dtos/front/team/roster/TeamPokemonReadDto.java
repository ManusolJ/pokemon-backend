package com.poketeambuilder.dtos.front.team.roster;

import com.poketeambuilder.dtos.front.item.ItemSummaryDto;
import com.poketeambuilder.dtos.front.nature.NatureReadDto;
import com.poketeambuilder.dtos.front.type.single.TypeReadDto;
import com.poketeambuilder.dtos.front.ability.AbilitySummaryDto;
import com.poketeambuilder.dtos.front.pokemon.form.PokemonSummaryDto;

import java.util.List;

/**
 * One configured slot of a team — Pokémon plus the build (level, nature, ability, held item,
 * tera type, EVs/IVs, and chosen moves). Maps 1:1 to the {@code team_pokemon} row.
 */
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
