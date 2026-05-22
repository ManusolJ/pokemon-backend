package com.poketeambuilder.utils.enums;

import com.poketeambuilder.entities.Nature;

/**
 * Stat identifier used by {@link Nature} (which stat a nature
 * boosts / hurts). Wire values match the PokeAPI stat names (hyphenated lowercase).
 */
public enum StatName implements ValuedEnum {

    HP("hp"),
    SPEED("speed"),
    ATTACK("attack"),
    DEFENSE("defense"),
    SPECIAL_ATTACK("special-attack"),
    SPECIAL_DEFENSE("special-defense");

    private final String value;

    StatName(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    /** Parses the PokeAPI string back to the enum. Case-insensitive. */
    public static StatName fromValue(String value) {
        for (StatName stat : values()) {
            if (stat.value.equalsIgnoreCase(value)) {
                return stat;
            }
        }

        throw new IllegalArgumentException("Unknown stat name: " + value);
    }
}
