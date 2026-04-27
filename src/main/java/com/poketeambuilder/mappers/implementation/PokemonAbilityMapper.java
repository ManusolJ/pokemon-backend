package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;

import com.poketeambuilder.entities.PokemonAbility;

import com.poketeambuilder.dtos.front.ability.AbilityEmbedDto;

import com.poketeambuilder.mappers.common.MapperConfiguration;

@Mapper(config = MapperConfiguration.class, uses = AbilityMapper.class)
public interface PokemonAbilityMapper {

    AbilityEmbedDto toEmbedDto(PokemonAbility entity);
}