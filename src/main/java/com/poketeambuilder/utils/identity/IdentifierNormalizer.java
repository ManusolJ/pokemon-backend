package com.poketeambuilder.utils.identity;

import java.util.Locale;

/**
 * Canonical forms for account identifiers. Emails are stored lower-cased so the stored value,
 * the {@code lower(email)} unique index and every lookup agree on what "the same address" means.
 */
public final class IdentifierNormalizer {

    private IdentifierNormalizer() {
    }

    /** Trims and lower-cases an email address. {@code null} passes through. */
    public static String email(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
