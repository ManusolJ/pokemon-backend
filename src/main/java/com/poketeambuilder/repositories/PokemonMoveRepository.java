package com.poketeambuilder.repositories;

import java.util.List;
import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poketeambuilder.entities.PokemonMove;
import com.poketeambuilder.entities.compositeIDs.PokemonMoveId;

/** CRUD + specification queries for the {@link PokemonMove} join table. */
public interface PokemonMoveRepository extends BaseRepository<PokemonMove, PokemonMoveId> {

    /**
     * Returns a page of moves known by the given Pokemon, including the per-row
     * {@code learn_method} / {@code level_learned_at} metadata. Backed by the {@code idx_pokemon_move_move}
     * index on the reverse direction; this direction uses the table primary key.
     *
     * @param pokemonId target Pokemon id (matches {@link PokemonMoveId#getPokemonId()})
     * @param pageable  paging / sorting
     * @return page of join rows
     */
    Page<PokemonMove> findByIdPokemonId(Integer pokemonId, Pageable pageable);

    /**
     * Of the supplied move ids, the subset the given Pokemon can actually learn. One statement
     * for a whole moveset, so validating a team save costs one query per slot rather than one
     * per move. A move appears under several learn methods, hence the {@code DISTINCT}.
     */
    @Query("""
            SELECT DISTINCT pm.id.moveId FROM PokemonMove pm
            WHERE pm.id.pokemonId = :pokemonId
              AND pm.id.moveId IN :moveIds
            """)
    List<Integer> findLearnableMoveIds(@Param("pokemonId") Integer pokemonId, @Param("moveIds") Collection<Integer> moveIds);
}
