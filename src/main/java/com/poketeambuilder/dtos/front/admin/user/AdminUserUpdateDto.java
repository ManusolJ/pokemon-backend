package com.poketeambuilder.dtos.front.admin.user;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;

/**
 * Admin-only user mutation. Every field is optional — only those set on the payload are
 * applied. Bounds mirror the {@code app_user} column widths.
 */
@Getter
public class AdminUserUpdateDto {

    @Pattern(regexp = "USER|ADMIN", message = "Role must be one of USER, ADMIN")
    private String newRole;

    private Boolean enabled;

    @Email
    @Size(max = 255)
    private String newEmail;

    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters long")
    private String newUsername;
}
