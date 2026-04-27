package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;

import com.poketeambuilder.entities.Type;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;

import com.poketeambuilder.dtos.pokeapi.type.TypeApiDto;

import com.poketeambuilder.mappers.common.ApiMapper;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface TypeMapper extends ReadMapper<Type, TypeReadDto>, ApiMapper<Type, TypeApiDto> {
    
    @Override
    TypeReadDto toReadDto(Type entity);

    @Override
    Type toEntity(TypeApiDto apiDto);
}
