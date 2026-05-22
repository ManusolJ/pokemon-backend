package com.poketeambuilder.repositories;

import java.util.Optional;

import com.poketeambuilder.entities.AppUser;

/**
 * CRUD + lookups for {@link AppUser}. Username and email are unique in the database, so the
 * {@code findBy…} methods return at most one row.
 *
 * <p>String comparisons are case-sensitive at the SQL level (Postgres default collation);
 * callers that need case-insensitive matching should normalise input upstream.</p>
 */
public interface UserRepository extends BaseRepository<AppUser, Long> {

    /** Looks up a user by exact username match. */
    Optional<AppUser> findByUsername(String username);

    /** Looks up a user by exact email match. */
    Optional<AppUser> findByEmail(String email);

    /** Returns {@code true} iff a user with the given username exists. */
    boolean existsByUsername(String username);

    /** Returns {@code true} iff a user with the given email exists. */
    boolean existsByEmail(String email);
}
