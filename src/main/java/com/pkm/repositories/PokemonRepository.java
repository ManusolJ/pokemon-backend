package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import com.pkm.entities.Pokemon;

public interface PokemonRepository extends JpaRepository<Pokemon, Long> {
    Optional<Pokemon> findByNameIgnoreCase(String name);

    Page<Pokemon> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Pokemon> findAllByType1Id(Long type, Pageable pageable);

    Page<Pokemon> findAllByType1IdAndType2Id(Long type1, Long type2, Pageable pageable);
}
