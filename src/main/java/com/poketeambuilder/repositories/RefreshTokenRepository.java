package com.poketeambuilder.repositories;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.Instant;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.RefreshToken;

/**
 * Storage for issued refresh tokens. Tokens are persisted as SHA-256 hashes and
 * grouped into rotation lineages via {@link RefreshToken#getFamilyId()} so detected reuse
 * can revoke an entire family at once.
 */
public interface RefreshTokenRepository extends BaseRepository<RefreshToken, Long> {

    /** Looks up a stored token by its SHA-256 hash. */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Returns every token in a rotation family. Used to revoke the whole lineage on reuse detection. */
    List<RefreshToken> findByFamilyId(UUID familyId);

    /** Returns every active or revoked token issued to a given user. */
    List<RefreshToken> findByUserId(Long userId);

    /**
     * Bulk-deletes tokens whose expiry has passed. Intended for the scheduled cleanup job.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff")
    void deleteByExpiresAtBefore(@Param("cutoff") Instant cutoff);
}
