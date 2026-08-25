package com.poketeambuilder.services.command;

import com.poketeambuilder.entities.AuditLog;

import com.poketeambuilder.repositories.AuditLogRepository;

import com.poketeambuilder.utils.enums.AuditAction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.verify;

/**
 * Audit writes must never be the thing that fails an audited operation.
 *
 * <p>{@code entity} and {@code entity_id} are {@code VARCHAR(50)}. Callers were passing a
 * formatted description into one and a whole list of ids into the other, so a long team name or a
 * batch of more than about a dozen users overran the column. The write runs in its own
 * transaction but the exception still propagates, which meant a team delete rolled back and a
 * batch delete committed with no record of it. Truncating is the lesser evil.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuditLogCommandServiceTest {

    private static final int COLUMN_WIDTH = 50;

    @Mock private AuditLogRepository auditLogRepository;

    @InjectMocks private AuditLogCommandService auditLogCommandService;

    @Captor private ArgumentCaptor<AuditLog> saved;

    @Test
    @DisplayName("An over-long entity reference is clipped rather than allowed to fail the insert")
    void clipsOverlongEntity() {
        auditLogCommandService.log("oak", AuditAction.ADMIN_TEAM_DELETE, "E".repeat(200), "1");

        assertThat(captured().getEntity()).hasSize(COLUMN_WIDTH);
    }

    @Test
    @DisplayName("An over-long entity id is clipped too")
    void clipsOverlongEntityId() {
        auditLogCommandService.log("oak", AuditAction.ADMIN_BATCH_HARD_DELETE, "User", "9".repeat(200));

        assertThat(captured().getEntityId()).hasSize(COLUMN_WIDTH);
    }

    @Test
    @DisplayName("Values that fit are stored untouched")
    void leavesShortValuesAlone() {
        auditLogCommandService.log("oak", AuditAction.ADMIN_USER_UPDATE, "User", "42", "changed role");

        AuditLog entry = captured();
        assertThat(entry.getEntity()).isEqualTo("User");
        assertThat(entry.getEntityId()).isEqualTo("42");
        assertThat(entry.getDetails()).isEqualTo("changed role");
    }

    @Test
    @DisplayName("Details are not clipped, since that column is TEXT")
    void keepsFullDetails() {
        String details = "D".repeat(4000);

        auditLogCommandService.log("oak", AuditAction.ADMIN_BATCH_SOFT_DELETE, "User", null, details);

        assertThat(captured().getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("A null reference stays null rather than becoming an empty string")
    void toleratesNullReferences() {
        auditLogCommandService.log("oak", AuditAction.ADMIN_BATCH_REACTIVATE, "User", null);

        assertThat(captured().getEntityId()).isNull();
    }

    private AuditLog captured() {
        verify(auditLogRepository).save(saved.capture());
        return saved.getValue();
    }
}
