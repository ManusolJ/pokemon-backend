package com.pkm.utils.enums;

import lombok.Getter;

@Getter
public enum MoveCategory {
    PHYSICAL("physical"),
    SPECIAL("special"),
    STATUS("status");

    private final String category;

    MoveCategory(String category) {
        this.category = category;
    }

    public static MoveCategory fromString(String category) {
        if (category != null) {
            for (MoveCategory moveCategory : MoveCategory.values()) {
                if (moveCategory.category.equals(category) || moveCategory.name().equals(category)) {
                    return moveCategory;
                }
            }
        }
        throw new IllegalArgumentException("Invalid category: " + category);
    }
}
