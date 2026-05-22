package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.poketeambuilder.entities.TeamPokemonMove;

import com.poketeambuilder.dtos.front.team.roster.TeamPokemonMoveEmbedDto;

import com.poketeambuilder.mappers.common.MapperConfiguration;

/**
 * Maps a {@link TeamPokemonMove} join row to its embed DTO. The {@code slot_position} half
 * of the composite key is lifted into a top-level field on the DTO.
 */
@Mapper(config = MapperConfiguration.class, uses = MoveMapper.class)
public interface TeamPokemonMoveMapper {

    @Mapping(target = "slotPosition", source = "id.slotPosition")
    TeamPokemonMoveEmbedDto toEmbedDto(TeamPokemonMove entity);
}
