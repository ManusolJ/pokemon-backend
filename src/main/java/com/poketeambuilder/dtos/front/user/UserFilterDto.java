package com.poketeambuilder.dtos.front.user;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import java.time.Instant;

import lombok.Getter;

/**
 * Filter payload for the admin user-listing endpoint. {@link #username} performs LIKE/contains;
 * {@link #usernameExact} forces exact match. {@link #createdAfter} / {@link #createdBefore}
 * apply an inclusive range on the user's creation timestamp.
 */
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
