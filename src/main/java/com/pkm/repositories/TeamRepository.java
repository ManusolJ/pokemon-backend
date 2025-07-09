package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pkm.entities.Team;

public interface TeamRepository extends JpaRepository<Team, Long>{
    
}
