package com.poketeambuilder.dtos.front.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordChangeDto {
    
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
    
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String currentPassword;
}
