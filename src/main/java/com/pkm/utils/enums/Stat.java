package com.pkm.utils.enums;

import lombok.Getter;

@Getter
public enum Stat {
    HP("HP", "hp"),
    ATTACK("Attack", "attack"),
    DEFENSE("Defense", "defense"),
    SP_ATTACK("Sp. Attack", "sp_attack"),
    SP_DEFENSE("Sp. Defense", "sp_defense"),
    SPEED("Speed", "speed");

    private final String displayName;
    private final String dbColumn;

    Stat(String displayName, String dbColumn) {
        this.displayName = displayName;
        this.dbColumn = dbColumn;
    }

    public static Stat fromDbColumn(String columnName) {
        for (Stat stat : values()) {
            if (stat.dbColumn.equalsIgnoreCase(columnName)) {
                return stat;
            }
        }
        throw new IllegalArgumentException("Invalid stat column: " + columnName);
    }
}