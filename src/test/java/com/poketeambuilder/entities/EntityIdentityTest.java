package com.poketeambuilder.entities;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Identity semantics for the two log entities.
 *
 * <p>Both carry {@code @EqualsAndHashCode(onlyExplicitlyIncluded = true)}, which makes Lombok
 * compare only the fields marked {@code @Include}. Neither marked any, so equals compared nothing
 * at all: every instance was equal to every other and hashCode was a constant. The rest of the
 * model keys on the identifier, and these now do too.</p>
 */
class EntityIdentityTest {

    @Test
    @DisplayName("Audit entries with different ids are different entries")
    void auditLogsAreDistinguishedById() {
        AuditLog first = AuditLog.builder().id(1L).username("oak").action("A").build();
        AuditLog second = AuditLog.builder().id(2L).username("oak").action("A").build();

        assertThat(first).isNotEqualTo(second);
        assertThat(Set.of(first, second)).hasSize(2);
    }

    @Test
    @DisplayName("Audit entries sharing an id are the same entry, whatever else differs")
    void auditLogsWithTheSameIdAreEqual() {
        AuditLog first = AuditLog.builder().id(7L).username("oak").action("A").build();
        AuditLog second = AuditLog.builder().id(7L).username("ash").action("B").build();

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Test
    @DisplayName("Seed runs with different ids are different runs")
    void seedLogsAreDistinguishedById() {
        SeedLog first = SeedLog.builder().id(1L).triggeredBy("oak").build();
        SeedLog second = SeedLog.builder().id(2L).triggeredBy("oak").build();

        assertThat(first).isNotEqualTo(second);
        assertThat(Set.of(first, second)).hasSize(2);
    }

    @Test
    @DisplayName("Seed runs sharing an id are the same run")
    void seedLogsWithTheSameIdAreEqual() {
        SeedLog first = SeedLog.builder().id(3L).triggeredBy("oak").build();
        SeedLog second = SeedLog.builder().id(3L).triggeredBy("ash").build();

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }
}
