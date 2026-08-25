package com.poketeambuilder.utils.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The two parse entry points differ on purpose, so the difference is worth pinning.
 *
 * <p>A value read out of the database that this build does not recognise should degrade to
 * {@link SeedStatus#UNKNOWN} — one odd row must not break the admin listing. A value supplied by
 * a caller should be rejected, because quietly reinterpreting a filter as {@code UNKNOWN} would
 * answer a bad request with an empty page rather than an error.</p>
 */
class SeedStatusTest {

    @ParameterizedTest
    @EnumSource(SeedStatus.class)
    @DisplayName("Every constant round-trips through its stored value")
    void roundTripsThroughStorageValue(SeedStatus status) {
        assertThat(SeedStatus.fromValue(status.getValue())).isEqualTo(status);
        assertThat(SeedStatus.fromDatabaseValue(status.getValue())).isEqualTo(status);
    }

    @Test
    @DisplayName("Stored values are matched without regard to case")
    void matchingIsCaseInsensitive() {
        assertThat(SeedStatus.fromValue("RUNNING")).isEqualTo(SeedStatus.RUNNING);
        assertThat(SeedStatus.fromValue("running")).isEqualTo(SeedStatus.RUNNING);
    }

    @Test
    @DisplayName("An unrecognised stored value degrades to UNKNOWN rather than throwing")
    void unrecognisedDatabaseValueDegrades() {
        assertThat(SeedStatus.fromDatabaseValue("Cancelled")).isEqualTo(SeedStatus.UNKNOWN);
        assertThat(SeedStatus.fromDatabaseValue("")).isEqualTo(SeedStatus.UNKNOWN);
    }

    @Test
    @DisplayName("An unrecognised caller value is rejected, not reinterpreted")
    void unrecognisedCallerValueIsRejected() {
        assertThatThrownBy(() -> SeedStatus.fromValue("Cancelled"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cancelled");
    }

    @Test
    @DisplayName("The pipeline never writes UNKNOWN itself; it is only ever a read fallback")
    void unknownIsAReadFallbackOnly() {
        assertThat(SeedStatus.UNKNOWN.getValue()).isEqualTo("Unknown");
        assertThat(SeedStatus.fromDatabaseValue("Unknown")).isEqualTo(SeedStatus.UNKNOWN);
    }
}
