package com.pkm.services.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort.Direction;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import com.pkm.DTOs.user.UserDTO;
import com.pkm.repositories.UserRepository;
import com.pkm.utils.enums.UserRole;
import com.pkm.utils.mappers.UserMapper;


/**
 * Service for querying user data with read-only transactions.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Retrieves a user by their unique ID.
     * 
     * @param id User ID (must be positive)
     * @return User DTO
     * @throws ResponseStatusException HTTP 404 if user not found
     */
    public UserDTO getUserById(@NotNull @Positive Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    String.format("User with ID '%d' not found.", id)));
    }

    /**
     * Retrieves a user by username (case-sensitive exact match).
     * 
     * @param username Username to search
     * @return User DTO
     * @throws ResponseStatusException HTTP 404 if user not found
     */
    public UserDTO getUserByUsername(@NotBlank String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    String.format("User with username '%s' not found.", username)));
    }

    /**
     * Retrieves paginated list of all users.
     * 
     * @param pageable Pagination configuration
     * @return Page of user DTOs
     */
    public Page<UserDTO> getUsersPage(
        @PageableDefault(size = 20, sort = "id", direction = Direction.ASC) Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDTO);
    }

    /**
     * Searches users by username substring (case-insensitive).
     * 
     * @param username Username search string
     * @param pageable Pagination configuration
     * @return Page of matching user DTOs
     */
    public Page<UserDTO> getUsersPageByUsername(
        @NotBlank String username, 
        @PageableDefault(size = 20, sort = "id", direction = Direction.ASC) Pageable pageable) {
        return userRepository.findAllByUsernameContainingIgnoreCase(username, pageable)
                .map(userMapper::toDTO);
    }

    /**
     * Filters users by role.
     * 
     * @param role User role to filter by
     * @param pageable Pagination configuration
     * @return Page of matching user DTOs
     */
    public Page<UserDTO> getUsersPageByRole(
        @NotBlank UserRole role, 
        @PageableDefault(size = 20, sort = "id", direction = Direction.ASC) Pageable pageable) {
        return userRepository.findAllByRole(role, pageable)
                .map(userMapper::toDTO);
    }

    /**
     * Retrieves users sorted by creation timestamp.
     * 
     * @param pageable Pagination configuration
     * @return Page of user DTOs sorted by creation time
     */
    public Page<UserDTO> getUsersPageByCreatedAt(
        @PageableDefault(size = 20, sort = "createdAt", direction = Direction.ASC) Pageable pageable) {
        return userRepository.findAllByCreatedAt(pageable)
                .map(userMapper::toDTO);
    }

    /**
     * Finds users created after specified timestamp.
     * 
     * @param createdAt Minimum creation timestamp
     * @param pageable Pagination configuration
     * @return Page of matching user DTOs
     */
    public Page<UserDTO> getUsersPageByCreatedAtAfter(
        @NotNull LocalDateTime createdAt, 
        @PageableDefault(size = 20, sort = "createdAt", direction = Direction.ASC) Pageable pageable) {
        return userRepository.findAllByCreatedAtAfter(createdAt, pageable)
                .map(userMapper::toDTO);
    }

    /**
     * Retrieves users sorted by last update timestamp.
     * 
     * @param pageable Pagination configuration
     * @return Page of user DTOs sorted by update time
     */
    public Page<UserDTO> getUsersPageByUpdatedAt(
        @PageableDefault(size = 20, sort = "updatedAt", direction = Direction.ASC) Pageable pageable) {
        return userRepository.findAllByUpdatedAt(pageable)
                .map(userMapper::toDTO);
    }
    
    /**
     * Finds users updated after specified timestamp.
     * 
     * @param updatedAt Minimum update timestamp
     * @param pageable Pagination configuration
     * @return Page of matching user DTOs
     */
    public Page<UserDTO> getUsersPageByUpdatedAtAfter(
        @NotNull LocalDateTime updatedAt, 
        @PageableDefault(size = 20, sort = "updatedAt", direction = Direction.ASC) Pageable pageable) {
        return userRepository.findAllByCreatedAtAfter(updatedAt, pageable)
                .map(userMapper::toDTO);
    }
}
