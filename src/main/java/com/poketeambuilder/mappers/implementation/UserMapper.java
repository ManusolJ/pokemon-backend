package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
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

/**
 * Maps {@link AppUser} between persistence and its front-end DTOs.
 *
 * <p>{@code toEntity(RegisterDto)} deliberately leaves {@code password} unset.
 */
@Mapper(config = MapperConfiguration.class)
public interface UserMapper extends ReadMapper<AppUser, UserReadDto>, SummaryMapper<AppUser, UserSummaryDto>, WriteMapper<AppUser, RegisterDto> {

    @Override
    UserReadDto toReadDto(AppUser entity);

    @Override
    UserSummaryDto toSummaryDto(AppUser entity);

    @Override
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    AppUser toEntity(RegisterDto dto);

    /** Applies a self-service profile update. Email and username are renamed from the DTO's {@code new*} fields. */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "email", source = "newEmail")
    @Mapping(target = "username", source = "newUsername")
    void updateEntity(UserUpdateDto dto, @MappingTarget AppUser entity);

    /** Applies an admin mutation. Adds the {@code role} field on top of the self-service set. */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "role", source = "newRole")
    @Mapping(target = "email", source = "newEmail")
    @Mapping(target = "username", source = "newUsername")
    void updateEntity(AdminUserUpdateDto dto, @MappingTarget AppUser entity);
}
