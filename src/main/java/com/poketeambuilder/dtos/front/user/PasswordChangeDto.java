package com.poketeambuilder.dtos.front.user;

import lombok.Getter;

@Getter
public class PasswordChangeDto {
    
    String newPassword;
    
    String currentPassword;
}
