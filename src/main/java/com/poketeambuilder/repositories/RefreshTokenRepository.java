package com.poketeambuilder.repositories;

import java.util.UUID;
import java.util.List;
import java.time.Instant;
import java.util.Optional;

import com.poketeambuilder.entities.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByFamilyId(UUID familyId);

    List<RefreshToken> findByUserId(Long userId);

    void deleteByExpiresAtBefore(Instant cutoff);
}