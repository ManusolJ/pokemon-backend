package com.poketeambuilder.repositories;

import java.time.Instant;
import java.util.Optional;

import com.poketeambuilder.entities.PasswordResetToken;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    void deleteByExpiresAtBefore(Instant cutoff);
    
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}