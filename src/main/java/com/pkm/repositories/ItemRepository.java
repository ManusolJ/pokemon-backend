package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import com.pkm.entities.Item;

/**
 * Repository interface for {@link Item} entities.
 *
 * Enables lookup by name (case-insensitive) and paginated searches.
 *
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Find an item by name, ignoring case.
     *
     * @param name the item name to match
     * @return an {@link Optional} containing the item if found
     */
    Optional<Item> findByNameIgnoreCase(String name);

    /**
     * Find items whose names contain the given substring (case-insensitive).
     *
     * @param name     partial name to search
     * @param pageable pagination information
     * @return a {@link Page} of matching items
     */
    Page<Item> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
