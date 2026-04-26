package com.poketeambuilder.mappers.nature;

import org.mapstruct.Mapper;

import com.poketeambuilder.entities.Nature;

import com.poketeambuilder.dtos.front.nature.NatureReadDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface NatureReadMapper extends ReadMapper<Nature, NatureReadDto> {
    
    @Override
    NatureReadDto toReadDto(Nature entity);
}
