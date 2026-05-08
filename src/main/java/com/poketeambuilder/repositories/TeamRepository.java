package com.poketeambuilder.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.Team;

public interface TeamRepository extends BaseRepository<Team, Long> {
    
    @Modifying
    @Query("UPDATE Team t SET t.likeCount = t.likeCount + 1 WHERE t.id = :teamId")
    void incrementLikeCount(@Param("teamId") Long teamId);

    @Modifying
    @Query("UPDATE Team t SET t.likeCount = t.likeCount - 1 WHERE t.id = :teamId AND t.likeCount > 0")
    void decrementLikeCount(@Param("teamId") Long teamId);
}
