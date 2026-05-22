package com.poketeambuilder.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.poketeambuilder.entities.Move;

/** CRUD + specification queries for {@link Move} reference data. */
public interface MoveRepository extends BaseRepository<Move, Integer> {

    /**
     * Returns just the ids of every persisted move. Used by the seed pipeline to do a
     * cheap "is this move id known?" membership check without loading entities.
     */
    @Query("SELECT m.id FROM Move m")
    List<Integer> findAllIds();
}
