package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import com.pkm.entities.Item;

public interface ItemRepository extends JpaRepository<Item, Long>{
    Optional<Item> findByNameIgnoreCase(String name);

    Page<Item> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
