package com.poketeambuilder.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.Team;
import com.poketeambuilder.entities.TeamLike;
import com.poketeambuilder.entities.compositeIDs.TeamLikeId;

public interface TeamLikeRepository extends BaseRepository<TeamLike, TeamLikeId> {
    
    @Modifying
    @Query("DELETE FROM TeamLike tl WHERE tl.tean = :team")
    void deleteByTeam(@Param("team") Team team);
}
