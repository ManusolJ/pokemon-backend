package com.poketeambuilder.services.command;

import com.poketeambuilder.entities.AuditLog;

import com.poketeambuilder.repositories.AuditLogRepository;

import com.poketeambuilder.utils.enums.AuditAction;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Persists {@link AuditLog} rows. Every {@code log(...)} call runs in its own independent
 * transaction ({@code REQUIRES_NEW}) so audit entries commit even when the surrounding
 * business transaction rolls back.
 */
@Service
@RequiredArgsConstructor
public class AuditLogCommandService {

    private final AuditLogRepository auditLogRepository;

    /** Persists an audit entry with optional structured details. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String username, AuditAction action, String entity, String entityId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action.getValue())
                .entity(entity)
                .entityId(entityId)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
    }

    /** Persists an audit entry without details. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String username, AuditAction action, String entity, String entityId) {
        log(username, action, entity, entityId, null);
    }
}
