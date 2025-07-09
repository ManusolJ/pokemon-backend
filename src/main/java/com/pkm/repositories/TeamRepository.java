package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pkm.entities.Team;

/**
 * Repository interface for {@link Team} entities.
 *
 * Provides CRUD operations and paging/sorting on the Team table.
 *
 */
public interface TeamRepository extends JpaRepository<Team, Long> {
    // No custom methods currently needed, but can be added later
}
