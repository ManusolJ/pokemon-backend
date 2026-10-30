package com.poketeambuilder.services.command;

import com.poketeambuilder.entities.AuditLog;

import com.poketeambuilder.repositories.AuditLogRepository;

import com.poketeambuilder.utils.enums.AuditAction;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Persists {@link AuditLog} rows. Each entry joins the caller's transaction, so it commits or
 * rolls back together with the change it records: the trail never claims something happened
 * that was undone. Callers whose rejection path must still leave a record (refresh-token reuse)
 * commit through that rejection rather than relying on a side transaction here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogCommandService {

    /** Mirrors {@code audit_log.entity} / {@code audit_log.entity_id} — {@code VARCHAR(50)}. */
    private static final int MAX_REFERENCE_LENGTH = 50;

    private final AuditLogRepository auditLogRepository;

    /** Persists an audit entry with optional structured details. */
    @Transactional
    public void log(String username, AuditAction action, String entity, String entityId, String details) {
        write(username, action, entity, entityId, details);
    }

    /** Persists an audit entry without details. */
    @Transactional
    public void log(String username, AuditAction action, String entity, String entityId) {
        write(username, action, entity, entityId, null);
    }

    /**
     * Shared body for both overloads.
     */
    private void write(String username, AuditAction action, String entity, String entityId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action.getValue())
                .entity(clip(entity, "entity"))
                .entityId(clip(entityId, "entityId"))
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
    }

    private String clip(String value, String field) {
        if (value == null || value.length() <= MAX_REFERENCE_LENGTH) {
            return value;
        }

        log.warn("Audit log {} exceeded {} characters and was truncated: {}", field, MAX_REFERENCE_LENGTH, value);
        return value.substring(0, MAX_REFERENCE_LENGTH);
    }
}
