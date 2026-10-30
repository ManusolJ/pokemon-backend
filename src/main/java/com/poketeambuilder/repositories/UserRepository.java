package com.poketeambuilder.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.poketeambuilder.entities.AppUser;

import com.poketeambuilder.utils.enums.UserRole;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD + lookups for {@link AppUser}. Username and email are unique, ignoring case, among
 * non-tombstoned rows in the database, so the {@code findBy…AndDeletedAtIsNull}
 * methods return at most one row.
 *
 * <p>The inherited {@code findById} does <strong>not</strong> filter on {@code deleted_at} —
 * admin paths that intentionally operate on tombstoned rows (hard-delete, restore) rely on
 * that. All authentication-bearing lookups go through the {@code …AndDeletedAtIsNull} methods.</p>
 */
public interface UserRepository extends BaseRepository<AppUser, Long> {

    /** Looks up an active user by id. The path every authenticated request resolves its caller through. */
    Optional<AppUser> findByIdAndDeletedAtIsNull(Long id);

    /** Looks up an active user by username, ignoring case. */
    Optional<AppUser> findByUsernameIgnoreCaseAndDeletedAtIsNull(String username);

    /** Looks up an active user by email, ignoring case. */
    Optional<AppUser> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    /** Returns {@code true} iff an active user holds the given username, ignoring case. */
    boolean existsByUsernameIgnoreCaseAndDeletedAtIsNull(String username);

    /** Returns {@code true} iff an active user holds the given email, ignoring case. */
    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    /**
     * Locks and returns the administrators who can still sign in. Guards the paths that could
     * otherwise lock everyone out: two admins demoting each other at once would each count two
     * admins and both succeed, so the count has to be taken under a row lock. The second caller
     * blocks until the first commits, and then no longer sees the demoted row.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM AppUser u WHERE u.role = :role AND u.enabled = true AND u.deletedAt IS NULL")
    List<AppUser> lockActiveByRole(@Param("role") UserRole role);

    /**
     * Hard-deletes tombstoned users whose {@code deleted_at} is strictly before the cutoff.
     * Returns the number of rows removed.
     *
     * <p>Intended for a scheduled cleanup task: pass {@code Instant.now().minus(N, DAYS)}
     * to enforce a grace window during which a self-deleted account could still be restored.
     * Pass {@code Instant.now()} to purge all tombstoned rows immediately.</p>
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM AppUser u WHERE u.deletedAt IS NOT NULL AND u.deletedAt < :cutoff")
    int purgeDeletedBefore(Instant cutoff);
}
