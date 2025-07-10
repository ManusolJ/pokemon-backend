package com.pkm.DTOs.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserDTO {

    // Unique identifier for the user
    @NotNull
    private Long id;

    // User's username, must not be blank
    @NotBlank
    private String username;

    // User's email address, must not be blank
    @NotBlank
    private String email;

    // User's profile photo, must not be blank
    @NotBlank
    private String profilePhoto;

    // User's role, must not be blank
    @NotBlank
    private String role;

    // User's creation timestamp, must not be null
    @NotNull
    private LocalDateTime createdAt;

    // User's last updated timestamp, must not be null
    @NotNull
    private LocalDateTime updatedAt;
}
