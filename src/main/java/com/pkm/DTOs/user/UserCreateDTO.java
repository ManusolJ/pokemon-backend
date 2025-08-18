package com.pkm.DTOs.user;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class UserCreateDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
