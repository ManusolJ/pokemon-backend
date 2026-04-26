package com.poketeambuilder.mappers.helpers.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EffectTextSubstitution {

    private static final String EFFECT_CHANCE_PLACEHOLDER = "$effect_chance";

    public static String substituteEffectChance(String text, Integer chance) {
        if (text == null) {
            return null;
        }

        if (chance == null) {
            return text;
        }
        
        return text.replace(EFFECT_CHANCE_PLACEHOLDER, String.valueOf(chance));
    }
}
