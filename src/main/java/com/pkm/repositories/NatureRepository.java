package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pkm.entities.Nature;

/**
 * Repository interface for {@link Nature} entities.
 *
 * Provides CRUD operations and paging/sorting on the Nature table.
 *
 */
public interface NatureRepository extends JpaRepository<Nature, Long> {
    // No custom methods currently needed, but can be added later
}
