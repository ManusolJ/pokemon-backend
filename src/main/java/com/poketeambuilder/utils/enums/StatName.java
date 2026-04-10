package com.poketeambuilder.utils.enums;

public enum StatName {
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

    public String getValue() {
        return value;
    }

    public static StatName fromValue(String value) {
        for (StatName stat : StatName.values()) {
            if (stat.value.equalsIgnoreCase(value)) {
                return stat;
            }
        }
        
        throw new IllegalArgumentException("Unknown stat name: " + value);
    }
}
