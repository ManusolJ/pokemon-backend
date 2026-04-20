package com.poketeambuilder.dtos.front.user;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class UserFilterDto implements FilterDtoInterface {

    private Long id;

    private String username;

    private String usernameExact;

    private String email;

    private String role;
    
    private Boolean enabled;
}