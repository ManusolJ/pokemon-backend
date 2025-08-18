package com.pkm.DTOs.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class UserDTO {
    // Unique identifier for the user
    @NotNull
    private Long id;

    // User's username, must not be blank
    @NotBlank
    private String username;

    // User's profile photo, must not be blank
    @NotBlank
    private String profilePhoto;

    // User's role, must not be blank
    @NotBlank
    private String role;
}
