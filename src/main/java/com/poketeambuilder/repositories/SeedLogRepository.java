package com.poketeambuilder.repositories;

import java.util.Optional;

import com.poketeambuilder.entities.SeedLog;

import com.poketeambuilder.utils.enums.SeedStatus;

/** CRUD + specification queries for {@link SeedLog} operational records. */
public interface SeedLogRepository extends BaseRepository<SeedLog, Long> {

    /** Returns the most-recently-started seed log row with the given status, if any. */
    Optional<SeedLog> findFirstByStatusOrderByStartedAtDesc(SeedStatus status);
}
