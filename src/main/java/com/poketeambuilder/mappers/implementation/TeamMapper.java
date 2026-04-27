package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.poketeambuilder.entities.Team;

import com.poketeambuilder.dtos.front.team.team.TeamReadDto;
import com.poketeambuilder.dtos.front.team.team.TeamCreateDto;
import com.poketeambuilder.dtos.front.team.team.TeamUpdateDto;
import com.poketeambuilder.dtos.front.team.team.TeamSummaryDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.SummaryMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

@Mapper(config = MapperConfiguration.class, uses = UserMapper.class)
public interface TeamMapper extends ReadMapper<Team, TeamReadDto>, SummaryMapper<Team, TeamSummaryDto> {

    @Override
    @Mapping(target = "pokemon", ignore = true)
    TeamReadDto toReadDto(Team entity);

    @Override
    @Mapping(target = "pokemonSprites", ignore = true)
    TeamSummaryDto toSummaryDto(Team entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Team toEntity(TeamCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TeamUpdateDto dto, @MappingTarget Team entity);
}