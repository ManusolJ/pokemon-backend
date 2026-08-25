package com.poketeambuilder.utils.enums;

import com.poketeambuilder.entities.TeamPokemon;

/**
 * Gender of a {@link TeamPokemon}.
 *
 *
 */
public enum PokemonGender implements ValuedEnum {

    MALE("male"),
    GENDERLESS("none"),
    FEMALE("female");

    private final String value;

    PokemonGender(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    /** Parses either the storage value ({@code "none"}) or the wire name ({@code "GENDERLESS"}). Case-insensitive. */
    public static PokemonGender fromValue(String value) {
        for (PokemonGender gender : values()) {
            if (gender.value.equalsIgnoreCase(value) || gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }

        throw new IllegalArgumentException("Unknown gender value: " + value);
    }
}
