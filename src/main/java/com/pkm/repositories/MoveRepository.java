package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pkm.entities.Move;

public interface MoveRepository extends JpaRepository<Move, Long> {
    Page<Move> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
