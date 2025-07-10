package com.pkm.DTOs.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDTO {

    @NotNull
    private Long id;

    @NotBlank
    private String username;

    @NotNull
    private String email;

    @NotBlank
    private String profilePhoto;

    @NotBlank
    private String role;
}
