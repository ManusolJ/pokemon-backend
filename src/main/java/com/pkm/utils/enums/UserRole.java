package com.pkm.utils.enums;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    USER("ROLE_USER"),
    MODERATOR("ROLE_MODERATOR"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

    public static UserRole fromString(String roleName) {
        if (roleName != null) {
            for (UserRole role : UserRole.values()) {
                if (role.authority.equals(roleName) || role.name().equals(roleName)) {
                    return role;
                }
            }
        }
        throw new IllegalArgumentException("Invalid role: " + roleName);
    }
}