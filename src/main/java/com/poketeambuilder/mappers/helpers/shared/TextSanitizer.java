package com.poketeambuilder.mappers.helpers.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextSanitizer {

    public static String clean(String text) {
        if (text == null) return null;
        return text.replace('\n', ' ')
                   .replace('\f', ' ')
                   .replaceAll("\\s+", " ")
                   .trim();
    }
}
