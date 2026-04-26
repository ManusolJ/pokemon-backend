package com.poketeambuilder.mappers.helpers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlavorTextSanitizer {

    public static String clean(String text) {
        if (text == null) return null;
        return text.replace('\n', ' ')
                   .replace('\f', ' ')
                   .replaceAll("\\s+", " ")
                   .trim();
    }
}
