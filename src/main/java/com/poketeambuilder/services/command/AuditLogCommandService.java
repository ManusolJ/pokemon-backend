package com.poketeambuilder.services.command;

import com.poketeambuilder.entities.AuditLog;

import com.poketeambuilder.repositories.AuditLogRepository;

import com.poketeambuilder.utils.enums.AuditAction;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogCommandService {

    private final AuditLogRepository auditLogRepository;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String username, AuditAction action, String entity, String entityId) {
        log(username, action, entity, entityId, null);
    }
}