package com.poketeambuilder.mappers.helpers.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Strips the PokeAPI sprite prefix from incoming URLs so we persist a relative
 * path instead of an absolute URL. The front-end re-attaches a CDN base, which
 * lets us swap the asset host without touching the database.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpriteUrlRewriter {

    private static final String GITHUB_PREFIX = "https://raw.githubusercontent.com/PokeAPI/sprites/master";

    /** Returns the URL with the GitHub prefix removed, or {@code null} when the input is blank. */
    public static String rewrite(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            return null;
        }

        return originalUrl.replace(GITHUB_PREFIX, "");
    }
}
