package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pkm.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    
    User findByUsername(String username);


}
