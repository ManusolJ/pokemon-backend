package com.poketeambuilder.dtos.front.user;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

import lombok.Getter;

@Getter
public class UserUpdateDto {
    
    @Email
    private String newEmail;
    
    @Size(min = 3, message = "Username must be at least 3 characters long")
    private String newUsername;
}
