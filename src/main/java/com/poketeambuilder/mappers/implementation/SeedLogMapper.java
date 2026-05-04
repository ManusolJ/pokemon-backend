package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;

import com.poketeambuilder.entities.SeedLog;

import com.poketeambuilder.dtos.front.admin.seed.SeedLogReadDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface SeedLogMapper extends ReadMapper<SeedLog, SeedLogReadDto>{
    
    @Override
    SeedLogReadDto toReadDto(SeedLog entity);
}
