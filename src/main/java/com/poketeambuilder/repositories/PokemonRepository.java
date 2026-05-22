package com.poketeambuilder.repositories;

import java.util.List;
import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.Pokemon;

/** CRUD + specification queries for {@link Pokemon} (form-level) reference data. */
public interface PokemonRepository extends BaseRepository<Pokemon, Integer> {

    /**
     * Loads the default-form Pokémon for each supplied species id, with both type references
     * fetched. Used by the Pokédex listing to enrich each species summary with its canonical types and sprite.
     */
    @Query("""
            SELECT p FROM Pokemon p
            JOIN FETCH p.primaryType
            LEFT JOIN FETCH p.secondaryType
            WHERE p.species.id IN :speciesIds
              AND p.isDefaultForm = true
            """)
    List<Pokemon> findDefaultFormsBySpeciesIdIn(@Param("speciesIds") Collection<Integer> speciesIds);
}
