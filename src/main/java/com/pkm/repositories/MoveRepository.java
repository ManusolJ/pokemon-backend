package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pkm.entities.Move;

/**
 * Repository interface for {@link Move} entities.
 *
 * Provides paginated search by move name (case-insensitive).
 *
 */
public interface MoveRepository extends JpaRepository<Move, Long> {

    /**
     * Find moves whose names contain the given substring (case-insensitive).
     *
     * @param name partial name to search
     * @param pageable pagination information
     * @return a {@link Page} of matching moves
     */
    Page<Move> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
