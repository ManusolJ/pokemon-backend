package com.poketeambuilder.repositories;

import java.util.List;
import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.Team;
import com.poketeambuilder.entities.TeamLike;
import com.poketeambuilder.entities.compositeIDs.TeamLikeId;

/**
 * CRUD + targeted reads for {@link TeamLike}. The team-listing read path uses
 * {@link #findLikedTeamIds(Long, Collection)} to mark which teams the caller has liked, in
 * one query, instead of N existence checks.
 */
public interface TeamLikeRepository extends BaseRepository<TeamLike, TeamLikeId> {

    /**
     * Bulk-deletes every like attached to the given team.
     */
    @Modifying
    @Query("DELETE FROM TeamLike tl WHERE tl.team = :team")
    void deleteByTeam(@Param("team") Team team);

    /**
     * Returns the subset of {@code teamIds} that the given user has liked. Used to enrich
     * team listings with a per-row "liked by me".
     */
    @Query("SELECT tl.team.id FROM TeamLike tl WHERE tl.user.id = :userId AND tl.team.id IN :teamIds")
    List<Long> findLikedTeamIds(@Param("userId") Long userId, @Param("teamIds") Collection<Long> teamIds);
}
