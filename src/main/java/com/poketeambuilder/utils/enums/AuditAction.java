package com.poketeambuilder.utils.enums;

import com.poketeambuilder.entities.AuditLog;

/**
 * Action identifier persisted on every {@link AuditLog} row.
 * Wire value equals {@link Enum#name()}; we never read the column back into the enum (audit
 * data is human-read only), so {@link #fromValue(String)} is intentionally omitted.
 *
 * <p>Naming convention: {@code USER_*} for self-service actions, {@code ADMIN_*} for
 * privileged actions, {@code ADMIN_BATCH_*} for bulk variants.</p>
 */
public enum AuditAction implements ValuedEnum {

    USER_SELF_DELETE,
    USER_PROFILE_UPDATE,
    USER_PASSWORD_CHANGE,

    ADMIN_TEAM_DELETE,
    ADMIN_USER_UPDATE,
    ADMIN_USER_REACTIVATE,
    ADMIN_USER_HARD_DELETE,
    ADMIN_USER_SOFT_DELETE,

    ADMIN_BATCH_SOFT_DELETE,
    ADMIN_BATCH_REACTIVATE,
    ADMIN_BATCH_HARD_DELETE,

    ADMIN_SEED_TRIGGERED;

    @Override
    public String getValue() {
        return name();
    }
}
