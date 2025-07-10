package com.pkm.utils.mappers;

import org.mapstruct.Mapper;

import com.pkm.entities.User;
import com.pkm.DTOs.user.UserDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);

    User toEntity(UserDTO userDTO);
}
