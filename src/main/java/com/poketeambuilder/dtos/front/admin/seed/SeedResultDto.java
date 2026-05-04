package com.poketeambuilder.dtos.front.admin.seed;

public record SeedResultDto(int entriesAdded, int errors) {

    public static SeedResultDto of(int entriesAdded, int errors) {
        return new SeedResultDto(entriesAdded, errors);
    }

    public SeedResultDto add(SeedResultDto other) {
        return new SeedResultDto(
            this.entriesAdded + other.entriesAdded,
            this.errors + other.errors
        );
    }
}