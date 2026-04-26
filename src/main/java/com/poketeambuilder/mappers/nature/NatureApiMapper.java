package com.poketeambuilder.mappers.nature;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.poketeambuilder.entities.Nature;

import com.poketeambuilder.dtos.pokeapi.nature.NatureApiDto;

import com.poketeambuilder.mappers.common.ApiMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;
import com.poketeambuilder.mappers.helpers.resource.NatureIngestionHelper;

@Mapper(config = MapperConfiguration.class, uses = NatureIngestionHelper.class)
public interface NatureApiMapper extends ApiMapper<NatureApiDto, Nature> {
    
    @Override
    @Mapping(target = "increasedStat", source = "increasedStat", qualifiedByName = "extractStatName")
    @Mapping(target = "decreasedStat", source = "decreasedStat", qualifiedByName = "extractStatName")
    Nature toEntity(NatureApiDto apiDto);
}
