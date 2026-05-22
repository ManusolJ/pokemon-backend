package com.poketeambuilder.mappers.helpers.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Resolves PokeAPI placeholder tokens in effect text. The most common case is move effects
 * that contain {@code $effect_chance}, which we replace with the move's actual
 * {@code effect_chance} value before persisting so the front-end displays a number instead
 * of the raw token.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EffectTextSubstitution {

    private static final String EFFECT_CHANCE_PLACEHOLDER = "$effect_chance";

    /**
     * Replaces {@code $effect_chance} with {@code chance} in {@code text}. Returns the input
     * unchanged when {@code chance} is null (e.g. the move has no secondary effect).
     */
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
