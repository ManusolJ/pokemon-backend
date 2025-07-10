package com.pkm.utils.enums;

import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;

/**
 * Represents user roles in the system with Spring Security integration.
 * Implements GrantedAuthority for role-based authorization.
 */
@Getter
public enum UserRole implements GrantedAuthority {
    /** Standard user permissions */
    USER("USER"),
    
    /** Moderator with elevated permissions */
    MODERATOR("MODERATOR"),
    
    /** Administrator with full system access */
    ADMIN("ADMIN");

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    /**
     * Converts a string representation to UserRole enum.
     * 
     * @param roleName Role name string (case-sensitive)
     * @return Corresponding UserRole
     * @throws IllegalArgumentException If invalid role name provided
     */
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