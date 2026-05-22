package com.poketeambuilder.repositories;

import java.util.List;
import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.Ability;
import com.poketeambuilder.entities.PokemonAbility;
import com.poketeambuilder.entities.compositeIDs.PokemonAbilityId;

/** CRUD + targeted reads for the {@link PokemonAbility} join table. */
public interface PokemonAbilityRepository extends BaseRepository<PokemonAbility, PokemonAbilityId> {

    /**
     * Loads every {@link PokemonAbility} row for the supplied Pokémon ids, with the
     * {@link Ability} already fetched. Single SQL statement;
     * used by the Pokémon listing read path to avoid an n+1 fetch over abilities.
     */
    @Query("""
            SELECT pa FROM PokemonAbility pa
            JOIN FETCH pa.ability
            WHERE pa.id.pokemonId IN :pokemonIds
            """)
    List<PokemonAbility> findByPokemonIdInWithAbility(@Param("pokemonIds") Collection<Integer> pokemonIds);
}
