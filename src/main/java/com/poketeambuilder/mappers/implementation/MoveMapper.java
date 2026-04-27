package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.poketeambuilder.entities.Move;

import com.poketeambuilder.dtos.front.move.MoveReadDto;
import com.poketeambuilder.dtos.front.move.MoveSummaryDto;

import com.poketeambuilder.dtos.pokeapi.move.MoveApiDto;

import com.poketeambuilder.mappers.common.ApiMapper;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.SummaryMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

import com.poketeambuilder.mappers.helpers.resource.MoveIngestionHelper;

@Mapper(config = MapperConfiguration.class, uses = { MoveIngestionHelper.class, TypeMapper.class })
public interface MoveMapper extends ReadMapper<Move, MoveReadDto>, ApiMapper<Move, MoveApiDto>, SummaryMapper<Move, MoveSummaryDto> {
    
    @Override
    MoveReadDto toReadDto(Move entity);

    @Override
    MoveSummaryDto toSummaryDto(Move entity);

    @Override
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "category", source = "damageClass", qualifiedByName = "extractCategory")
    @Mapping(target = "effectDescription", source = ".", qualifiedByName = "extractEffectDescription")
    Move toEntity(MoveApiDto apiDto);
}
