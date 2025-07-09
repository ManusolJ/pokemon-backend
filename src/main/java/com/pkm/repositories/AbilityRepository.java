package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import com.pkm.entities.Ability;

public interface AbilityRepository extends JpaRepository<Ability, Long> {
    Optional<Ability> findByNameIgnoreCase(String name);

    Page<Ability> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}