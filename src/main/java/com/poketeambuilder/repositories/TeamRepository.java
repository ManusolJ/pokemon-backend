package com.poketeambuilder.repositories;

import java.util.Optional;

import com.poketeambuilder.entities.Team;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * CRUD + specification queries for {@link Team}. The denormalised {@link Team#getLikeCount()}
 * is maintained by a database trigger on {@code team_like} (V26), so nothing here writes it.
 */
public interface TeamRepository extends BaseRepository<Team, Long> {

    /** Whether the team exists and its owner hasn't been tombstoned. Gates the public single-team read. */
    boolean existsByIdAndOwnerDeletedAtIsNull(Long id);

    /**
     * Resolves a share link to its team, public or private, unless the owner has been tombstoned.
     * Backs the anonymous read-only share view.
     */
    @Query("SELECT t.id FROM Team t WHERE t.slug = :slug AND t.owner.deletedAt IS NULL")
    Optional<Long> findShareableIdBySlug(@Param("slug") String slug);
}
