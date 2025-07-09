package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pkm.entities.Type;

public interface TypeRepository extends JpaRepository<Type, Long> {
    
}
