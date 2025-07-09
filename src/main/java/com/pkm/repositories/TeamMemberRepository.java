package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pkm.entities.TeamMember;

/**
 * Repository interface for {@link TeamMember} entities.
 *
 * Provides CRUD operations and paging/sorting on the TeamMember table.
 *
 */
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    // No custom methods currently needed, but can be added later
}
