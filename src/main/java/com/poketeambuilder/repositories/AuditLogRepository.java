package com.poketeambuilder.repositories;

import com.poketeambuilder.entities.AuditLog;

/** CRUD + specification queries for {@link AuditLog} append-only records. */
public interface AuditLogRepository extends BaseRepository<AuditLog, Long> {
    
}
