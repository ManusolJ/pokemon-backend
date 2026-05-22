package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.poketeambuilder.entities.PokemonMove;

import com.poketeambuilder.dtos.front.move.MoveEmbedDto;

import com.poketeambuilder.mappers.common.MapperConfiguration;

/**
 * Maps a {@link PokemonMove} join row to its embed DTO. The {@code learn_method} half of the
 * composite key is lifted into a top-level field on the DTO.
 */
@Mapper(config = MapperConfiguration.class, uses = MoveMapper.class)
public interface PokemonMoveMapper {

    @Mapping(target = "learnMethod", source = "id.learnMethod")
    MoveEmbedDto toEmbedDto(PokemonMove entity);
}
