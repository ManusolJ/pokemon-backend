package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.poketeambuilder.entities.TeamPokemonMove;

import com.poketeambuilder.dtos.front.team.pokemon.TeamPokemonMoveEmbedDto;

import com.poketeambuilder.mappers.common.MapperConfiguration;

@Mapper(config = MapperConfiguration.class, uses = MoveMapper.class)
public interface TeamPokemonMoveMapper {

    @Mapping(target = "slotPosition", source = "id.slotPosition")
    TeamPokemonMoveEmbedDto toEmbedDto(TeamPokemonMove entity);
}