package com.poketeambuilder.utils.enums;

public enum PokemonGender {
    MALE("male"),
    NONE("none"),
    FEMALE("female");

    private final String value;

    PokemonGender(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PokemonGender fromValue(String value) {
        for (PokemonGender gender : PokemonGender.values()) {
            if (gender.value.equalsIgnoreCase(value)) {
                return gender;
            }
        }
        
        throw new IllegalArgumentException("Unknown gender value: " + value);
    }

}
