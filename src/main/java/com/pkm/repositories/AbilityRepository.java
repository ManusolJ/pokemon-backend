package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import com.pkm.entities.Ability;

/**
 * Repository interface for {@link Ability} entities.
 *
 * Enables lookup by name (case-insensitive) and paginated searches.
 *
 */
public interface AbilityRepository extends JpaRepository<Ability, Long> {

    /**
     * Find an ability by name, ignoring case.
     *
     * @param name the ability name to match
     * @return an {@link Optional} containing the ability if found
     */
    Optional<Ability> findByNameIgnoreCase(String name);

    /**
     * Find abilities whose names contain the given substring (case-insensitive).
     *
     * @param name     partial name to search
     * @param pageable pagination information
     * @return a {@link Page} of matching abilities
     */
    Page<Ability> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
