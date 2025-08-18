package com.pkm.utils.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.pkm.entities.User;
import com.pkm.DTOs.user.UserDTO;
import com.pkm.DTOs.user.UserResponseDTO;
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

    void updateUserFromDto(UserDTO dto, @MappingTarget User user);

    /**
     * Converts a User entity to a UserResponseDTO, including access and refresh
     * tokens.
     *
     * @param user         the User entity to convert
     * @param accessToken  the access token to include in the response
     * @param refreshToken the refresh token to include in the response
     * @return the converted UserResponseDTO
     */
    @Mapping(target = "role", source = "role", qualifiedByName = "roleToString")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    UserResponseDTO toResponseDTO(User user, String accessToken, String refreshToken);

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

    /**
     * Helper: Converts a role string to UserRole enum.
     *
     * @param role String representation of the role
     * @return Corresponding UserRole or null if invalid
     */
    private UserRole convertStringToRole(String role) {
        if (role == null)
            return null;
        for (UserRole r : UserRole.values()) {
            if (r.getAuthority().equals(role) || r.name().equalsIgnoreCase(role)) {
                return r;
            }
        }
        return null; // Invalid role string
    }

    /**
     * Converts a role string to UserRole enum. Uses the existing role if conversion
     * fails.
     * Executed automatically after the main update mapping.
     *
     * @param dto  Source DTO
     * @param user Target User entity being updated
     */
    @AfterMapping
    default void updateRoleFromDto(UserDTO dto, @MappingTarget User user) {
        if (dto.getRole() != null) {
            UserRole newRole = convertStringToRole(dto.getRole());
            if (newRole != null) {
                user.setRole(newRole);
            }
            // If invalid role, retain the existing user.getRole()
        }
    }

}
