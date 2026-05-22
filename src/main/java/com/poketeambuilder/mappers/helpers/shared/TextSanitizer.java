package com.poketeambuilder.mappers.helpers.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Single-pass cleaner for PokeAPI text fields — collapses CR/LF/form-feeds and any run of
 * whitespace into a single space, then trims. Used by {@link TextExtractor} so persisted
 * flavor and effect strings render predictably on the front-end.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextSanitizer {

    /** Returns the cleaned single-line form of {@code text}, or {@code null} when the input is null. */
    public static String clean(String text) {
        if (text == null) {
            return null;
        }

        return text.replace('\n', ' ')
                   .replace('\f', ' ')
                   .replaceAll("\\s+", " ")
                   .trim();
    }
}
