package com.poketeambuilder.dtos.front.admin.seed;

public record SeedResult(int entriesAdded, int errors) {

    public static SeedResult of(int entriesAdded, int errors) {
        return new SeedResult(entriesAdded, errors);
    }

    public SeedResult add(SeedResult other) {
        return new SeedResult(
            this.entriesAdded + other.entriesAdded,
            this.errors + other.errors
        );
    }
}