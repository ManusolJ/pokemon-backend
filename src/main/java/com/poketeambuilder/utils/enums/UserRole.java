package com.poketeambuilder.utils.enums;

import com.poketeambuilder.entities.AppUser;

/**
 * Authorization role of an {@link AppUser}. Wire value equals
 * {@link Enum#name()}, kept as a {@link ValuedEnum} for converter symmetry, so the security
 * layer can serialize the role uniformly with every other value-backed enum.
 */
public enum UserRole implements ValuedEnum {

    USER,
    ADMIN;

    @Override
    public String getValue() {
        return name();
    }

    /** Parses the stored string back to the enum. Case-insensitive. */
    public static UserRole fromValue(String value) {
        for (UserRole role : values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Unknown user role: " + value);
    }
}
