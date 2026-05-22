package com.poketeambuilder.utils.enums;

import com.poketeambuilder.entities.TeamPokemon;

/**
 * Gender of a {@link TeamPokemon}. {@link #NONE} represents
 * genderless species (Magnemite, Voltorb, etc); PokeAPI sends lowercase strings.
 */
public enum PokemonGender implements ValuedEnum {

    MALE("male"),
    NONE("none"),
    FEMALE("female");

    private final String value;

    PokemonGender(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    /** Parses the string back to the enum. Case-insensitive. */
    public static PokemonGender fromValue(String value) {
        for (PokemonGender gender : values()) {
            if (gender.value.equalsIgnoreCase(value)) {
                return gender;
            }
        }

        throw new IllegalArgumentException("Unknown gender value: " + value);
    }
}
