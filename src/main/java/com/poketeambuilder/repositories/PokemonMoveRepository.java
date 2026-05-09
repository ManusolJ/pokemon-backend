package com.poketeambuilder.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.poketeambuilder.entities.PokemonMove;
import com.poketeambuilder.entities.compositeIDs.PokemonMoveId;

public interface PokemonMoveRepository extends BaseRepository<PokemonMove, PokemonMoveId>{
    
    Page<PokemonMove> findByIdPokemonId(Integer pokemonId, Pageable pageable);
}
