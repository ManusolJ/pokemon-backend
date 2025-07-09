package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pkm.entities.TeamMember;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long>{
    
}
