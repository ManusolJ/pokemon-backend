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
     * Bulk-revokes every token in the given rotation family in a single UPDATE. Avoids the
     * load-and-iterate pattern when token-reuse is detected and we need to invalidate the
     * whole lineage immediately.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.familyId = :familyId")
    int revokeByFamilyId(@Param("familyId") UUID familyId);

    /**
     * Bulk-revokes every token issued to the given user in a single UPDATE. Used on password
     * change, account disable, and password reset. Invalidates every active session.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    int revokeByUserId(@Param("userId") Long userId);

    /**
     * Bulk-deletes tokens whose expiry has passed. Intended for the scheduled cleanup job.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff")
    void deleteByExpiresAtBefore(@Param("cutoff") Instant cutoff);
}
