package com.pkm.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.pkm.entities.User;
import com.pkm.DTOs.user.UserDTO;
import com.pkm.utils.enums.UserRole;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Converts a User entity to a UserDTO.
     *
     * @param user the User entity to convert
     * @return the converted UserDTO
     */
    @Mapping(target = "role", source = "role", qualifiedByName = "roleToString")
    UserDTO toDTO(User user);

    /**
     * Converts a UserRole enum to its string representation.
     *
     * @param role the UserRole to convert
     * @return the string representation of the UserRole
     */
    @Named("roleToString")
    default String roleToString(UserRole role) {
        return role != null ? role.getAuthority() : null;
    }
}
