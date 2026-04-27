package com.poketeambuilder.mappers.implementation;

import com.poketeambuilder.entities.Ability;

import com.poketeambuilder.dtos.front.ability.AbilityReadDto;
import com.poketeambuilder.dtos.front.ability.AbilitySummaryDto;

import com.poketeambuilder.dtos.pokeapi.ability.AbilityApiDto;

import com.poketeambuilder.mappers.common.ApiMapper;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.SummaryMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

import com.poketeambuilder.mappers.helpers.shared.TextExtractor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class, uses = { TextExtractor.class })
public interface AbilityMapper extends ReadMapper<Ability, AbilityReadDto>, ApiMapper<Ability, AbilityApiDto>, SummaryMapper<Ability, AbilitySummaryDto>{
    
    @Override
    AbilityReadDto toReadDto(Ability entity);

    @Override
    AbilitySummaryDto toSummaryDto(Ability entity);

    @Override
    @Mapping(target = "effect", source = "effectEntries", qualifiedByName = "extractEffect")
    @Mapping(target = "shortEffect", source = "effectEntries", qualifiedByName = "extractShortEffect")
    @Mapping(target = "flavorText", source = "flavorTextEntries", qualifiedByName = "extractAbilityFlavorText")
    Ability toEntity(AbilityApiDto apiDto);
}
