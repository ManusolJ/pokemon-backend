package com.poketeambuilder.utils.converters;

import com.poketeambuilder.utils.enums.UserRole;

import jakarta.persistence.Converter;

/** JPA converter for {@link UserRole} columns. Activated globally via {@code autoApply}. */
@Converter(autoApply = true)
public class UserRoleConverter extends ValuedEnumConverter<UserRole> {

    @Override
    protected UserRole fromValue(String value) {
        return UserRole.fromValue(value);
    }
}
