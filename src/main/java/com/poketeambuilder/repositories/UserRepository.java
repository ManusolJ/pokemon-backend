package com.poketeambuilder.repositories;

import java.time.Instant;
import java.util.Optional;

import com.poketeambuilder.entities.AppUser;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * CRUD + lookups for {@link AppUser}. Username and email are unique <em>among non-tombstoned
 * rows</em> in the database (see V23 partial indexes), so the {@code findBy…AndDeletedAtIsNull}
 * methods return at most one row.
 *
 * <p>String comparisons are case-sensitive at the SQL level (Postgres default collation);
 * callers that need case-insensitive matching should normalise input upstream.</p>
 *
 * <p>The inherited {@code findById} does <strong>not</strong> filter on {@code deleted_at} —
 * admin paths that intentionally operate on tombstoned rows (hard-delete, restore) rely on
 * that. All authentication-bearing lookups go through the {@code …AndDeletedAtIsNull} methods.</p>
 */
public interface UserRepository extends BaseRepository<AppUser, Long> {

    /** Looks up an active user by exact username match. */
    Optional<AppUser> findByUsernameAndDeletedAtIsNull(String username);

    /** Looks up an active user by exact email match. */
    Optional<AppUser> findByEmailAndDeletedAtIsNull(String email);

    /** Returns {@code true} iff an active user with the given username exists. */
    boolean existsByUsernameAndDeletedAtIsNull(String username);

    /** Returns {@code true} iff an active user with the given email exists. */
    boolean existsByEmailAndDeletedAtIsNull(String email);

    /**
     * Hard-deletes tombstoned users whose {@code deleted_at} is strictly before the cutoff.
     * Returns the number of rows removed.
     *
     * <p>Intended for a scheduled cleanup task: pass {@code Instant.now().minus(N, DAYS)}
     * to enforce a grace window during which a self-deleted account could still be restored.
     * Pass {@code Instant.now()} to purge all tombstoned rows immediately.</p>
     */
    @Modifying
    @Query("DELETE FROM AppUser u WHERE u.deletedAt IS NOT NULL AND u.deletedAt < :cutoff")
    int purgeDeletedBefore(Instant cutoff);
}
