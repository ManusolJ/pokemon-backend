package com.poketeambuilder.utils.enums;

import java.util.Optional;

import com.poketeambuilder.entities.SeedLog;

/**
 * Lifecycle state of a {@link SeedLog} row.
 *
 * <p>Wire values are Title-cased ({@code "Running"}, {@code "Completed"}, ...) to match the DB.
 * Constants follow the usual Java convention; the {@link #getValue()} mapping bridges the two.</p>
 *
 * <p>{@link #UNKNOWN} is the fallback for a stored value this build does not recognise - a row
 * written by a newer version, or edited by hand. It is never produced by the seed pipeline, which
 * only ever writes {@code RUNNING}, {@code COMPLETED} or {@code FAILED}. The two parse entry
 * points differ deliberately: {@link #fromDatabaseValue(String)} degrades to {@code UNKNOWN} so a
 * single odd row cannot break the admin listing, while {@link #fromValue(String)} still rejects
 * an unrecognised string, because silently reinterpreting a caller's filter as {@code UNKNOWN}
 * would answer a bad request with an empty result set instead of an error.</p>
 */
public enum SeedStatus implements ValuedEnum {

    FAILED("Failed"),
    RUNNING("Running"),
    UNKNOWN("Unknown"),
    COMPLETED("Completed");

    private final String value;

    SeedStatus(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    /** Strict parse for caller-supplied input. Case-insensitive. Throws on anything unrecognised. */
    public static SeedStatus fromValue(String value) {
        return resolve(value).orElseThrow(
                () -> new IllegalArgumentException("Unknown seed status: " + value));
    }

    /** Lenient parse for values read back out of the database. Falls back to {@link #UNKNOWN}. */
    public static SeedStatus fromDatabaseValue(String value) {
        return resolve(value).orElse(UNKNOWN);
    }

    private static Optional<SeedStatus> resolve(String value) {
        for (SeedStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return Optional.of(status);
            }
        }

        return Optional.empty();
    }
}
