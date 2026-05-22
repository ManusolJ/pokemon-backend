package com.poketeambuilder.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.Team;

/**
 * CRUD + specification queries for {@link Team}. Includes atomic counters for the
 * denormalised {@link Team#getLikeCount()}; both clear the persistence context so callers
 * that re-read the team in the same transaction see the new value.
 */
public interface TeamRepository extends BaseRepository<Team, Long> {

    /** Atomically increments {@link Team#getLikeCount()} by one. Paired with {@code TeamLike} insert. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Team t SET t.likeCount = t.likeCount + 1 WHERE t.id = :teamId")
    void incrementLikeCount(@Param("teamId") Long teamId);

    /**
     * Atomically decrements {@link Team#getLikeCount()} by one, but never below zero — guards
     * against double-decrement races if a delete is replayed.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Team t SET t.likeCount = t.likeCount - 1 WHERE t.id = :teamId AND t.likeCount > 0")
    void decrementLikeCount(@Param("teamId") Long teamId);
}
