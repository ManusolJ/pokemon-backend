package com.poketeambuilder.infrastructure.security;

import java.util.List;
import java.util.Collection;

import com.poketeambuilder.entities.AppUser;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.Getter;
import lombok.EqualsAndHashCode;

/**
 * Security principal for the API. Carries the user's immutable id next to the username, and
 * request handling resolves the caller by that id.
 *
 * <p>The username is display-only here: it can be changed, and a tombstoned account's name
 * can be registered again, so a name inside a still-valid access token could otherwise end up
 * resolving to a different account than the one the token was issued to.</p>
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class AuthenticatedUser extends User {

    private final Long id;

    public AuthenticatedUser(Long id, String username, String password, boolean enabled,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
    }

    /**
     * Builds the principal for a loaded account. The authority list carries a single
     * {@code ROLE_<role>} entry and the enabled flag is wired through, so a disabled account
     * fails authentication.
     */
    public static AuthenticatedUser of(AppUser user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getEnabled()),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getValue())));
    }
}
