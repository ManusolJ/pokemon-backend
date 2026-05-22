package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;

import com.poketeambuilder.entities.TeamPokemon;

import com.poketeambuilder.dtos.front.team.roster.TeamPokemonReadDto;
import com.poketeambuilder.dtos.front.team.roster.TeamPokemonCreateDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.WriteMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

/**
 * Maps {@link TeamPokemon} between persistence and its front-end DTOs.
 */
@Mapper(config = MapperConfiguration.class, uses = {
    PokemonMapper.class,
    AbilityMapper.class,
    NatureMapper.class,
    ItemMapper.class,
    TypeMapper.class
})
public interface TeamPokemonMapper extends ReadMapper<TeamPokemon, TeamPokemonReadDto>, WriteMapper<TeamPokemon, TeamPokemonCreateDto> {

    @Override
    @Mapping(target = "moves", ignore = true)
    TeamPokemonReadDto toReadDto(TeamPokemon entity);

    @Override
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "nickname", source = "nickname")
    @Mapping(target = "level", source = "level")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "shiny", source = "shiny")
    @Mapping(target = "evHp", source = "evHp")
    @Mapping(target = "evAtk", source = "evAtk")
    @Mapping(target = "evDef", source = "evDef")
    @Mapping(target = "evSpAtk", source = "evSpAtk")
    @Mapping(target = "evSpDef", source = "evSpDef")
    @Mapping(target = "evSpeed", source = "evSpeed")
    @Mapping(target = "ivHp", source = "ivHp")
    @Mapping(target = "ivAtk", source = "ivAtk")
    @Mapping(target = "ivDef", source = "ivDef")
    @Mapping(target = "ivSpAtk", source = "ivSpAtk")
    @Mapping(target = "ivSpDef", source = "ivSpDef")
    @Mapping(target = "ivSpeed", source = "ivSpeed")
    TeamPokemon toEntity(TeamPokemonCreateDto dto);
}
