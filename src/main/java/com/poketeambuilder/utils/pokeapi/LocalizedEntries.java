package com.poketeambuilder.utils.pokeapi;

import java.util.List;
import java.util.Optional;

import com.poketeambuilder.interfaces.LocalizedEntry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Helpers for picking a single localized entry out of a list of translations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalizedEntries {

    private static final String ENGLISH = "en";

    /** Returns the English entry, if any. */
    public static <T extends LocalizedEntry> Optional<T> english(List<T> entries) {
        return pickLanguage(entries, ENGLISH);
    }

    /** Returns the first entry matching the given language name (e.g. {@code "en"}, {@code "ja"}). */
    public static <T extends LocalizedEntry> Optional<T> pickLanguage(List<T> entries, String languageName) {
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }

        return entries.stream()
                .filter(e -> e.language() != null && languageName.equals(e.language().name()))
                .findFirst();
    }
}
