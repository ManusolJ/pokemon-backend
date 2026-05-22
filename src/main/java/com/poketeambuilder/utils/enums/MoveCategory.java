package com.poketeambuilder.utils.enums;

import com.poketeambuilder.entities.Move;

/**
 * Damage class of a {@link Move}. Matches the PokeAPI
 * {@code damage_class} string and the {@code chk_move_category} CHECK constraint in the DB
 * ({@code 'physical'}, {@code 'special'}, {@code 'status'}).
 */
public enum MoveCategory implements ValuedEnum {

    STATUS("status"),
    SPECIAL("special"),
    PHYSICAL("physical");

    private final String value;

    MoveCategory(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    /** Parses the PokeAPI string back to the enum. Case-insensitive. */
    public static MoveCategory fromValue(String value) {
        for (MoveCategory category : values()) {
            if (category.value.equalsIgnoreCase(value)) {
                return category;
            }
        }

        throw new IllegalArgumentException("Unknown move category: " + value);
    }
}
