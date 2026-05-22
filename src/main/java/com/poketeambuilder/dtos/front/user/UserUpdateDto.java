package com.poketeambuilder.dtos.front.user;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

import lombok.Getter;

/**
 * Self-service user-profile update. Both fields are optional; an unset field means "leave
 * unchanged". Sizes mirror the database column widths.
 */
@Getter
public class UserUpdateDto {

    @Email
    @Size(max = 255)
    private String newEmail;

    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters long")
    private String newUsername;
}
