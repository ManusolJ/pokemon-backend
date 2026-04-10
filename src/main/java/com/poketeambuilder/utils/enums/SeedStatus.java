package com.poketeambuilder.utils.enums;

public enum SeedStatus {
    FAILED("Failed"),
    RUNNING("Running"),
    UNKNOWN("Unknown"),
    COMPLETED("Completed");

    SeedStatus(String value) {
        this.value = value;
    }

    private final String value;

    public String getValue() {
        return value;
    }

    public static SeedStatus fromValue(String value) {
        for (SeedStatus status : SeedStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        
        throw new IllegalArgumentException("Unknown seed status: " + value);
    }
}
