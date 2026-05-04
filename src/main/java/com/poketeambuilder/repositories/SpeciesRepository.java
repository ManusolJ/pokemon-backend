package com.poketeambuilder.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import com.poketeambuilder.entities.PokemonSpecies;

public interface SpeciesRepository extends BaseRepository<PokemonSpecies, Integer> {
    
    @Modifying
    @Query("UPDATE PokemonSpecies s SET s.previousEvolution = null")
    void clearPreviousEvolutions();
}