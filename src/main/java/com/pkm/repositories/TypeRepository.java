package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pkm.entities.Type;

/**
 * Repository interface for {@link Type} entities.
 *
 * Provides CRUD operations and paging/sorting on the Type table.
 *
 */
public interface TypeRepository extends JpaRepository<Type, Long> {
    // No custom methods currently needed, but can be added later
}
