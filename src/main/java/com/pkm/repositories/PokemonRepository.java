package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import com.pkm.entities.Pokemon;

/**
 * Repository interface for {@link Pokemon} entities.
 *
 * Enables lookup by name, type filters, and paginated searches.
 *
 */
public interface PokemonRepository extends JpaRepository<Pokemon, Long> {

    /**
     * Find a Pokémon by name, ignoring case.
     *
     * @param name the Pokémon name to match
     * @return an {@link Optional} containing the Pokémon if found
     */
    Optional<Pokemon> findByNameIgnoreCase(String name);

    /**
     * Find Pokémon whose names contain the given substring (case-insensitive).
     *
     * @param name partial name to search
     * @param pageable pagination information
     * @return a {@link Page} of matching Pokémon
     */
    Page<Pokemon> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find Pokémon with a specific primary type.
     *
     * @param type the type ID to match
     * @param pageable pagination information
     * @return a {@link Page} of matching Pokémon
     */
    Page<Pokemon> findAllByType1Id(Long type, Pageable pageable);

    /**
     * Find Pokémon matching both primary and secondary types.
     *
     * @param type1 the primary type ID
     * @param type2 the secondary type ID
     * @param pageable pagination information
     * @return a {@link Page} of matching Pokémon
     */
    Page<Pokemon> findAllByType1IdAndType2Id(Long type1, Long type2, Pageable pageable);
}
