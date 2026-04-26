package com.poketeambuilder.mappers.helpers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpriteUrlRewriter {

    private static final String GITHUB_PREFIX = "https://raw.githubusercontent.com/PokeAPI/sprites/master";

    public static String rewrite(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            return null;
        }

        return originalUrl.replace(GITHUB_PREFIX, "");
    }
}
