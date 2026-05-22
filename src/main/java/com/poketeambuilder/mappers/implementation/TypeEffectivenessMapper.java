package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;

import com.poketeambuilder.entities.TypeEffectiveness;

import com.poketeambuilder.dtos.front.type.effectiveness.TypeEffectivenessReadDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

/** Maps {@link TypeEffectiveness} rows to their read DTO. */
@Mapper(config = MapperConfiguration.class)
public interface TypeEffectivenessMapper extends ReadMapper<TypeEffectiveness, TypeEffectivenessReadDto> {
}
