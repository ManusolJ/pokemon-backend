package com.poketeambuilder.dtos.front.admin.user;

import lombok.Getter;

@Getter
public class AdminUserUpdateDto {
    
    private String newRole;
    
    private Boolean enabled;
    
    private String newEmail;
    
    private String newUsername;
}
