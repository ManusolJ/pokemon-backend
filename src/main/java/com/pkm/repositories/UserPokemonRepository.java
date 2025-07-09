package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pkm.entities.UserPokemon;

/**
 * Repository interface for {@link UserPokemon} entities.
 *
 * Provides CRUD operations and paging/sorting on the UserPokemon table.
 *
 */
public interface UserPokemonRepository extends JpaRepository<UserPokemon, Long> {
    // No custom methods currently needed, but can be added later
}
