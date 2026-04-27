package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.poketeambuilder.entities.Ability;

import com.poketeambuilder.dtos.front.ability.AbilityReadDto;
import com.poketeambuilder.dtos.front.ability.AbilitySummaryDto;

import com.poketeambuilder.dtos.pokeapi.ability.AbilityApiDto;

import com.poketeambuilder.mappers.common.ApiMapper;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.SummaryMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

import com.poketeambuilder.mappers.helpers.resource.AbilityIngestionHelper;

@Mapper(config = MapperConfiguration.class, uses = { AbilityIngestionHelper.class })
public interface AbilityMapper extends ReadMapper<Ability, AbilityReadDto>, ApiMapper<Ability, AbilityApiDto>, SummaryMapper<Ability, AbilitySummaryDto>{
    
    @Override
    AbilityReadDto toReadDto(Ability entity);

    @Override
    AbilitySummaryDto toSummaryDto(Ability entity);

    @Override
    @Mapping(target = "effect", source = "effectEntries", qualifiedByName = "extractAbilityEffect")
    @Mapping(target = "description", source = "flavorTextEntries", qualifiedByName = "extractAbilityDescription")
    Ability toEntity(AbilityApiDto apiDto);
}
