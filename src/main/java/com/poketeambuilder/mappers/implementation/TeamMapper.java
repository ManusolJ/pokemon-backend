package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import com.poketeambuilder.entities.Team;

import com.poketeambuilder.dtos.front.team.details.TeamReadDto;
import com.poketeambuilder.dtos.front.team.details.TeamCreateDto;
import com.poketeambuilder.dtos.front.team.details.TeamUpdateDto;
import com.poketeambuilder.dtos.front.team.details.TeamSummaryDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.WriteMapper;
import com.poketeambuilder.mappers.common.SummaryMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

/**
 * Maps {@link Team} between persistence and its various front-end DTOs.
 */
@Mapper(config = MapperConfiguration.class, uses = UserMapper.class)
public interface TeamMapper extends ReadMapper<Team, TeamReadDto>, SummaryMapper<Team, TeamSummaryDto>, WriteMapper<Team, TeamCreateDto> {

    @Override
    @Mapping(target = "pokemon", ignore = true)
    @Mapping(target = "likedByCurrentUser", ignore = true)
    TeamReadDto toReadDto(Team entity);

    @Override
    @Mapping(target = "pokemonSprites", ignore = true)
    @Mapping(target = "likedByCurrentUser", ignore = true)
    TeamSummaryDto toSummaryDto(Team entity);

    @Override
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "isPublic", source = "isPublic")
    Team toEntity(TeamCreateDto dto);

    /** Applies a {@link TeamUpdateDto} on top of an existing team. Only name and visibility move. */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "isPublic", source = "isPublic")
    void updateEntity(TeamUpdateDto dto, @MappingTarget Team entity);
}
