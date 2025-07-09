package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

import com.pkm.entities.User;
import com.pkm.utils.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);
    
    Page<User> findAllByUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<User> findAllByCreatedAtAfter(LocalDateTime createdAt, Pageable pageable);

    Page<User> findAllByUpdatedAtAfter(LocalDateTime updatedAt, Pageable pageable);

    Page<User> findAllByRole(UserRole role, Pageable pageable);
}
