package com.poketeambuilder.dtos.REST.common;

import java.util.List;
import java.util.Optional;

import com.poketeambuilder.interfaces.LocalizedEntry;

public final class LocalizedEntries {

    private LocalizedEntries() {}
    
    private static final String ENGLISH = "en";

    public static <T extends LocalizedEntry> Optional<T> english(List<T> entries) {
        return pickLanguage(entries, ENGLISH);
    }

    public static <T extends LocalizedEntry> Optional<T> pickLanguage(List<T> entries, String languageName) {
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        
        return entries.stream()
                .filter(e -> e.language() != null && languageName.equals(e.language().name()))
                .findFirst();
    }
}
