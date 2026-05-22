package com.poketeambuilder.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.poketeambuilder.entities.PokemonMove;
import com.poketeambuilder.entities.compositeIDs.PokemonMoveId;

/** CRUD + specification queries for the {@link PokemonMove} join table. */
public interface PokemonMoveRepository extends BaseRepository<PokemonMove, PokemonMoveId> {

    /**
     * Returns a page of moves known by the given Pokémon, including the per-row
     * {@code learn_method} / {@code level_learned_at} metadata. Backed by the {@code idx_pokemon_move_move}
     * index on the reverse direction; this direction uses the table primary key.
     *
     * @param pokemonId target Pokémon id (matches {@link PokemonMoveId#getPokemonId()})
     * @param pageable  paging / sorting
     * @return page of join rows
     */
    Page<PokemonMove> findByIdPokemonId(Integer pokemonId, Pageable pageable);
}
