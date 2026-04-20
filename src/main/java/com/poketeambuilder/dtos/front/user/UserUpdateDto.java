package com.poketeambuilder.dtos.front.user;

import lombok.Getter;

@Getter
public class UserUpdateDto {
    boolean enabled;

    String newEmail;
    
    String newUsername;
}
