package com.poketeambuilder.dtos.front.team.roster;

import com.poketeambuilder.dtos.front.item.ItemSummaryDto;
import com.poketeambuilder.dtos.front.nature.NatureReadDto;
import com.poketeambuilder.dtos.front.type.single.TypeReadDto;
import com.poketeambuilder.dtos.front.ability.AbilitySummaryDto;
import com.poketeambuilder.dtos.front.pokemon.form.PokemonSummaryDto;

import java.util.List;

/**
 * One configured slot of a team — Pokemon plus the build (level, nature, ability, held item,
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
) {

    /** Copy with the move list filled in; the mapper leaves it unset and the query layer loads it in batch. */
    public TeamPokemonReadDto withMoves(List<TeamPokemonMoveEmbedDto> loadedMoves) {
        return new TeamPokemonReadDto(
                id, slot, nickname, level, gender, shiny,
                pokemon, ability, nature, heldItem, teraType,
                evHp, evAtk, evDef, evSpAtk, evSpDef, evSpeed,
                ivHp, ivAtk, ivDef, ivSpAtk, ivSpDef, ivSpeed,
                loadedMoves);
    }
}
