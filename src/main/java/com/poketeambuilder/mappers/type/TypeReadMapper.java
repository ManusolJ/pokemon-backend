package com.poketeambuilder.mappers.type;

import org.mapstruct.Mapper;

import com.poketeambuilder.entities.Type;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

@Mapper(config =  MapperConfiguration.class)
public interface TypeReadMapper  extends ReadMapper<Type, TypeReadDto> {
    
    @Override
    TypeReadDto toReadDto(Type entity);
}
