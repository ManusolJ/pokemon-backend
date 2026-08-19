package com.poketeambuilder.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.SeedLog;

import com.poketeambuilder.utils.enums.SeedStatus;

/** CRUD + specification queries for {@link SeedLog} operational records. */
public interface SeedLogRepository extends BaseRepository<SeedLog, Long> {

    /** Returns the most-recently-started seed log row with the given status, if any. */
    Optional<SeedLog> findFirstByStatusOrderByStartedAtDesc(SeedStatus status);

    /**
     * Closes off every row still marked running, returning how many were affected. Called on
     * startup to clear runs orphaned by a restart. Bulk update, so {@code @PreUpdate} doesn't
     * fire and {@code completed_at} is stamped here instead.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE SeedLog s
            SET s.status = :failed,
                s.completedAt = CURRENT_TIMESTAMP
            WHERE s.status = :running
            """)
    int failRunningLogs(@Param("failed") SeedStatus failed, @Param("running") SeedStatus running);
}
