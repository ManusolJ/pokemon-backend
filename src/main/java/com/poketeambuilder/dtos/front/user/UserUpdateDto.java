package com.poketeambuilder.dtos.front.user;

import com.poketeambuilder.infrastructure.validation.annotations.MaxUtf8Bytes;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

/**
 * Self-service user-profile update. Both identifier fields are optional; an unset field means
 * "leave unchanged". Sizes mirror the database column widths.
 *
 * <p>{@link #currentPassword} is required: the email address is where password resets go, so
 * letting a bare access token change it would turn any leaked token into a permanent account
 * takeover. Bounded the same way as {@link PasswordChangeDto#getCurrentPassword()}.</p>
 */
@Getter
public class UserUpdateDto {

    @NotBlank
    @MaxUtf8Bytes(MaxUtf8Bytes.BCRYPT_MAX)
    private String currentPassword;

    @Email
    @Size(max = 255)
    private String newEmail;

    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters long")
    private String newUsername;
}
