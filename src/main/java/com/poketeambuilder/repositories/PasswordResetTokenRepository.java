package com.poketeambuilder.repositories;

import java.time.Instant;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.PasswordResetToken;

/**
 * Storage for single-use password reset tokens. Persisted as SHA-256 hashes; once
 * {@link PasswordResetToken#isUsed()} flips, the same hash cannot be redeemed twice.
 */
public interface PasswordResetTokenRepository extends BaseRepository<PasswordResetToken, Long> {

    /** Looks up a stored token by its SHA-256 hash; the plain value is never persisted. */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** How many tokens the user has been issued since {@code since}. Backs the per-account request throttle. */
    long countByUserIdAndCreatedAtAfter(Long userId, Instant since);

    /**
     * Marks a token used only if it is still unused. Returns {@code 1} for the caller that won,
     * {@code 0} for a token already spent - including by a concurrent confirmation.
     */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.id = :id AND prt.used = false")
    int markUsedIfUnused(@Param("id") Long id);

    /** Spends every outstanding token of a user, so only the newest link (or none) still works. */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.user.id = :userId AND prt.used = false")
    int invalidateOutstandingForUser(@Param("userId") Long userId);

    /**
     * Bulk-deletes tokens whose expiry has passed. Intended for the scheduled cleanup job.
     * Bypasses the persistence context; caller must be {@code @Transactional}.
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :cutoff")
    void deleteByExpiresAtBefore(@Param("cutoff") Instant cutoff);
}
