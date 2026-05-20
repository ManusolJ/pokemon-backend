package com.poketeambuilder.dtos.front.user;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import java.time.Instant;

import lombok.Getter;

@Getter
public class UserFilterDto implements FilterDtoInterface {

    private Long id;

    private String username;

    private String usernameExact;

    private String email;

    private String role;

    private Boolean enabled;

    private Instant createdAfter;

    private Instant createdBefore;
}