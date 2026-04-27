package com.poketeambuilder.repositories;

import java.util.Optional;

import com.poketeambuilder.entities.AppUser;

public interface AppUserRepository extends BaseRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}