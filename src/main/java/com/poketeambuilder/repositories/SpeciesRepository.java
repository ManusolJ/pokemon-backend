package com.poketeambuilder.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import com.poketeambuilder.entities.PokemonSpecies;

/** CRUD + specification queries for {@link PokemonSpecies}. */
public interface SpeciesRepository extends BaseRepository<PokemonSpecies, Integer> {

    /**
     * Clears every species' {@code previous_evolution_id}. Used by the seed pipeline to break
     * the self-referencing FK cycle before re-ingesting evolution data so rows can be
     * inserted in any order without violating FK constraints.
     */
    @Modifying
    @Query("UPDATE PokemonSpecies s SET s.previousEvolution = null")
    void clearPreviousEvolutions();
}
