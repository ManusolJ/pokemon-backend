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

    /**
     * Bulk-deletes tokens whose expiry has passed. Intended for the scheduled cleanup job.
     * Bypasses the persistence context; caller must be {@code @Transactional}.
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :cutoff")
    void deleteByExpiresAtBefore(@Param("cutoff") Instant cutoff);
}
