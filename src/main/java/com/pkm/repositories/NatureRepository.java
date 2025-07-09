package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pkm.entities.Nature;

public interface NatureRepository extends JpaRepository<Nature, Long> {
    
}
