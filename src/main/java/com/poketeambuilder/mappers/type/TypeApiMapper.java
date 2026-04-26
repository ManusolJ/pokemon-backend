package com.poketeambuilder.mappers.type;

import org.mapstruct.Mapper;

import com.poketeambuilder.dtos.pokeapi.type.TypeApiDto;
import com.poketeambuilder.entities.Type;
import com.poketeambuilder.mappers.common.ApiMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface TypeApiMapper extends ApiMapper<TypeApiDto, Type> {
    
    @Override
    Type toEntity(TypeApiDto apiDto);
}
