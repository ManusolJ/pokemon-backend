package com.poketeambuilder.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import com.poketeambuilder.entities.PokemonSpecies;

/** CRUD + specification queries for {@link PokemonSpecies}. */
public interface SpeciesRepository extends BaseRepository<PokemonSpecies, Integer> {

    /**
     * Clears every species' {@code previous_evolution_id}. The seed pipeline runs this before
     * re-applying evolution links, so a species whose chain changed upstream doesn't keep
     * pointing at its old pre-evolution.
     */
    @Modifying
    @Query("UPDATE PokemonSpecies s SET s.previousEvolution = null")
    void clearPreviousEvolutions();
}
