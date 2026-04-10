package com.poketeambuilder.utils.enums;

public enum MoveCategory {
    STATUS("status"),
    SPECIAL("special"),
    PHYSICAL("physical");

    private final String value;

    MoveCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MoveCategory fromValue(String value) {
        for (MoveCategory category : values()) {
            if (category.value.equals(value)) {
                return category;
            }
        }
        
        throw new IllegalArgumentException("Unknown move category: " + value);
    }
}
