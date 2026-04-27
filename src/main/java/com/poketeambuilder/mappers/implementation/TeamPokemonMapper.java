package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.poketeambuilder.entities.TeamPokemon;

import com.poketeambuilder.dtos.front.team.pokemon.TeamPokemonReadDto;
import com.poketeambuilder.dtos.front.team.pokemon.TeamPokemonCreateDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.WriteMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

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
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "slot", ignore = true)
    @Mapping(target = "pokemon", ignore = true)
    @Mapping(target = "ability", ignore = true)
    @Mapping(target = "nature", ignore = true)
    @Mapping(target = "heldItem", ignore = true)
    @Mapping(target = "teraType", ignore = true)
    TeamPokemon toEntity(TeamPokemonCreateDto dto);
}