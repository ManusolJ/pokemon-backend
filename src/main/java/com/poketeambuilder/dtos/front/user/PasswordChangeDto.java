package com.poketeambuilder.dtos.front.user;

import com.poketeambuilder.infrastructure.validation.annotations.MaxUtf8Bytes;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

/**
 * Self-service password change. {@link #currentPassword} has no minimum — a user's existing
 * password may pre-date a stricter rule — only bcrypt's 72-byte input cap, which the encoder
 * enforces by throwing. {@link #newPassword} is held to the same cap in bytes as well as the
 * 72-character rule, since a character count alone lets multibyte input through.
 */
@Getter
public class PasswordChangeDto {

    @NotBlank
    @MaxUtf8Bytes(MaxUtf8Bytes.BCRYPT_MAX)
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters long")
    @MaxUtf8Bytes(value = MaxUtf8Bytes.BCRYPT_MAX, message = "Password must be at most 72 bytes long; non-ASCII characters take more than one")
    private String newPassword;
}
