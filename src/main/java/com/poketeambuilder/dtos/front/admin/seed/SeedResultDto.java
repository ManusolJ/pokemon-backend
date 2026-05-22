package com.poketeambuilder.dtos.front.admin.seed;

/**
 * Aggregate result of a seed step — how many rows were ingested and how many failed.
 * {@link #(SeedResultDto)} lets the orchestrator combine per-resource counts into a single
 * run total without mutating its inputs.
 */
public record SeedResultDto(int entriesAdded, int errors) {

    /** Convenience factory mirroring {@code new SeedResultDto(…)} for fluent call sites. */
    public static SeedResultDto of(int entriesAdded, int errors) {
        return new SeedResultDto(entriesAdded, errors);
    }

    /** Returns a new total combining this result with {@code other}. Both inputs are unchanged. */
    public SeedResultDto add(SeedResultDto other) {
        return new SeedResultDto(
            this.entriesAdded + other.entriesAdded,
            this.errors + other.errors
        );
    }
}
