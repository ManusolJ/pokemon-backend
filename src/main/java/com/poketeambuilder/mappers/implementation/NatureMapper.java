package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.poketeambuilder.entities.Nature;

import com.poketeambuilder.dtos.front.nature.NatureReadDto;

import com.poketeambuilder.dtos.pokeapi.nature.NatureApiDto;

import com.poketeambuilder.mappers.common.ApiMapper;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

import com.poketeambuilder.mappers.helpers.resource.NatureIngestionHelper;

@Mapper(config = MapperConfiguration.class, uses = { NatureIngestionHelper.class })
public interface NatureMapper extends ReadMapper<Nature, NatureReadDto>, ApiMapper<Nature, NatureApiDto> {
    
    @Override
    NatureReadDto toReadDto(Nature entity);

    @Override
    @Mapping(target = "increasedStat", source = "increasedStat", qualifiedByName = "extractStatName")
    @Mapping(target = "decreasedStat", source = "decreasedStat", qualifiedByName = "extractStatName")
    Nature toEntity(NatureApiDto apiDto);
}
