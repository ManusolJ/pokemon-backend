package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.poketeambuilder.entities.AppUser;

import com.poketeambuilder.dtos.auth.RegisterDto;

import com.poketeambuilder.dtos.front.user.UserReadDto;
import com.poketeambuilder.dtos.front.user.UserUpdateDto;
import com.poketeambuilder.dtos.front.user.UserSummaryDto;

import com.poketeambuilder.dtos.front.admin.user.AdminUserUpdateDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.SummaryMapper;
import com.poketeambuilder.mappers.common.WriteMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface UserMapper extends ReadMapper<AppUser, UserReadDto>, SummaryMapper<AppUser, UserSummaryDto>, WriteMapper<AppUser, RegisterDto> {

    @Override
    UserReadDto toReadDto(AppUser entity);

    @Override
    UserSummaryDto toSummaryDto(AppUser entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    AppUser toEntity(RegisterDto dto);

    @Mapping(target = "email", source = "newEmail")
    @Mapping(target = "username", source = "newUsername")
    void updateEntity(UserUpdateDto dto, @MappingTarget AppUser entity);

    @Mapping(target = "role", source = "newRole")
    @Mapping(target = "email", source = "newEmail")
    @Mapping(target = "username", source = "newUsername")
    void updateEntity(AdminUserUpdateDto dto, @MappingTarget AppUser entity);
}