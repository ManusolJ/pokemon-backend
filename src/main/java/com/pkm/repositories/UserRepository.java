package com.pkm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

import com.pkm.entities.User;
import com.pkm.utils.enums.UserRole;

/**
 * Repository interface for {@link User} entities.
 *
 * Extends standard CRUD with username lookups, case-insensitive searches,
 * timestamp filters, role filtering, and paginated results.
 *
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Determine if a user exists with the given username.
     *
     * @param username the username to check
     * @return {@code true} if a user with that username exists; {@code false} otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Retrieve a user by exact username match.
     *
     * @param username the username to search
     * @return an {@link Optional} describing the found user, or empty if none found
     */
    Optional<User> findByUsername(String username);

    /**
     * Retrieve a user by username ignoring case.
     *
     * @param username the username to search (case-insensitive)
     * @return an {@link Optional} describing the found user, or empty if none found
     */
    Optional<User> findByUsernameIgnoreCase(String username);

    /**
     * Find users whose usernames contain the specified substring (case-insensitive).
     *
     * @param username partial username to match
     * @param pageable pagination information
     * @return a {@link Page} of matching users
     */
    Page<User> findAllByUsernameContainingIgnoreCase(String username, Pageable pageable);

    /**
     * Find users created after the given timestamp.
     *
     * @param createdAt the lower bound for creation time
     * @param pageable pagination information
     * @return a {@link Page} of users created after the timestamp
     */
    Page<User> findAllByCreatedAtAfter(LocalDateTime createdAt, Pageable pageable);

    /**
     * Find users updated after the given timestamp.
     *
     * @param updatedAt the lower bound for update time
     * @param pageable pagination information
     * @return a {@link Page} of users updated after the timestamp
     */
    Page<User> findAllByUpdatedAtAfter(LocalDateTime updatedAt, Pageable pageable);

    /**
     * Find users by their role.
     *
     * @param role the {@link UserRole} to match
     * @param pageable pagination information
     * @return a {@link Page} of users with the specified role
     */
    Page<User> findAllByRole(UserRole role, Pageable pageable);
}
