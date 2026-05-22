package com.poketeambuilder.utils.enums;

import com.poketeambuilder.entities.SeedLog;

/**
 * Lifecycle state of a {@link SeedLog} row.
 *
 * <p>Wire values are Title-cased ({@code "Running"}, {@code "Completed"}, …) to match the DB.
 * Constants follow the usual Java convention; the {@link #getValue()} mapping bridges the two.</p>
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

    /** Parses the stored string back to the enum. Case-insensitive. */
    public static SeedStatus fromValue(String value) {
        for (SeedStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unknown seed status: " + value);
    }
}
