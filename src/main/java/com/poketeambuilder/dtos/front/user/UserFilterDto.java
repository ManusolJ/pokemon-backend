package com.poketeambuilder.dtos.front.user;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import java.time.Instant;

import jakarta.validation.constraints.Pattern;

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

    /** Matched case-insensitively against {@link com.poketeambuilder.utils.enums.UserRole}. */
    @Pattern(regexp = "USER|ADMIN", flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "Role must be one of USER, ADMIN")
    private String role;

    private Boolean enabled;

    private Instant createdAfter;

    private Instant createdBefore;

    private Boolean includeDeleted;
}
