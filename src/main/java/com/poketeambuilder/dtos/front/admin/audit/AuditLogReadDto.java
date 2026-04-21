package com.poketeambuilder.dtos.front.admin.audit;

import java.time.Instant;

public record AuditLogReadDto(
    long id,
    String username,
    String action,
    String entity,
    String entityId,
    String details,
    Instant createdAt
) {}