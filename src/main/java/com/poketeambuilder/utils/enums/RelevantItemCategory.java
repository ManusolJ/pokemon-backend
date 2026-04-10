package com.poketeambuilder.utils.enums;

public enum RelevantItemCategory {
    PLATES("plates"),
    CHOICE("choice"),
    BERRIES("berries"),
    IN_A_PINCH("in-a-pinch"),
    HELD_ITEMS("held-items"),
    Z_CRYSTALS("z-crystals"),
    TERA_SHARD("tera-shard"),
    MEGA_STONES("mega-stones"),
    PICKY_HEALING("picky-healing"),
    BAD_HELD_ITEMS("bad-held-items"),
    TYPE_PROTECTION("type-protection"),
    TYPE_ENHANCEMENT("type-enhancement");

    private final String apiValue;

    RelevantItemCategory(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static boolean isRelevant(String category) {
        for (RelevantItemCategory c : values()) {
            if (c.apiValue.equals(category)) {
                return true;
            }
        }
        return false;
    }
}
