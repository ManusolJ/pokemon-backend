package com.poketeambuilder.services.command;

import java.time.Instant;
import java.util.List;

import com.poketeambuilder.entities.AppUser;

import com.poketeambuilder.dtos.front.user.UserReadDto;
import com.poketeambuilder.dtos.front.user.UserUpdateDto;
import com.poketeambuilder.dtos.front.user.PasswordChangeDto;

import com.poketeambuilder.dtos.front.admin.user.AdminUserUpdateDto;

import com.poketeambuilder.infrastructure.exceptions.BadPasswordException;
import com.poketeambuilder.infrastructure.exceptions.ResourceNotFoundException;
import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;

import com.poketeambuilder.mappers.implementation.UserMapper;

import com.poketeambuilder.repositories.UserRepository;

import com.poketeambuilder.services.auth.RefreshTokenService;

import com.poketeambuilder.utils.enums.AuditAction;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mutating operations on {@link AppUser} rows. Every flow that disables an account or
 * changes credentials revokes the user's refresh tokens via {@link RefreshTokenService}
 * so old sessions can't continue refreshing after the security-relevant change.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserCommandService {

    private static final String ENTITY_NAME = "User";

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogCommandService auditLogCommandService;

    /** Self-service profile update. Email and username are checked for uniqueness before save. */
    @Transactional
    public UserReadDto updateProfile(@NotNull String username, @Valid @NotNull UserUpdateDto dto) {
        AppUser user = findUserOrThrowByUsername(username);

        validateEmailUniqueness(dto.getNewEmail(), user);
        validateUsernameUniqueness(dto.getNewUsername(), user);

        userMapper.updateEntity(dto, user);

        AppUser saved = userRepository.save(user);

        auditLogCommandService.log(saved.getUsername(), AuditAction.USER_PROFILE_UPDATE, ENTITY_NAME, saved.getId().toString());

        return userMapper.toReadDto(saved);
    }

    /**
     * Self-service password change. Validates the current password, persists the new hash,
     * and revokes every active refresh token so old sessions die immediately.
     */
    @Transactional
    public void changePassword(@NotNull String username, @Valid @NotNull PasswordChangeDto dto) {
        AppUser user = findUserOrThrowByUsername(username);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BadPasswordException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        userRepository.save(user);

        refreshTokenService.revokeAllForUser(user.getId());

        auditLogCommandService.log(user.getUsername(), AuditAction.USER_PASSWORD_CHANGE, ENTITY_NAME, user.getId().toString());
    }

    /**
     * Self-service account disable. Tombstones the row (frees username/email for reuse),
     * disables it, and revokes every active session.
     */
    @Transactional
    public void softDeleteAccount(@NotNull String username) {
        AppUser user = findUserOrThrowByUsername(username);

        tombstone(user);

        auditLogCommandService.log(user.getUsername(), AuditAction.USER_SELF_DELETE, ENTITY_NAME, user.getId().toString());
    }

    /** Admin update of a user's role/email/username/enabled flag. Uniqueness validated as for self-service. */
    @Transactional
    public UserReadDto adminUpdateUser(@NotNull String adminUsername, @NotNull Long userId, @Valid @NotNull AdminUserUpdateDto dto) {
        AppUser user = findUserOrThrow(userId);

        validateEmailUniqueness(dto.getNewEmail(), user);
        validateUsernameUniqueness(dto.getNewUsername(), user);

        userMapper.updateEntity(dto, user);

        AppUser saved = userRepository.save(user);

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_USER_UPDATE, ENTITY_NAME, userId.toString());

        return userMapper.toReadDto(saved);
    }

    /** Admin soft-delete. Tombstones the row and revokes every active session. */
    @Transactional
    public void adminSoftDeleteUser(@NotNull String adminUsername, @NotNull Long userId) {
        AppUser user = findUserOrThrow(userId);

        String originalUsername = user.getUsername();
        tombstone(user);

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_USER_SOFT_DELETE, ENTITY_NAME, userId.toString(),
                "Soft-deleted user: " + originalUsername);
    }

    /**
     * Admin reactivation. Clears the tombstone and re-enables the account, but only after
     * verifying the user's original username and email aren't already held by an active row
     * (since the partial unique index would reject the save otherwise).
     */
    @Transactional
    public UserReadDto adminReactivateUser(@NotNull String adminUsername, @NotNull Long userId) {
        AppUser user = findUserOrThrow(userId);

        if (isTombstoned(user)) {
            assertIdentifiersStillFree(user);
        }
        restore(user);

        AppUser saved = userRepository.save(user);

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_USER_REACTIVATE, ENTITY_NAME, userId.toString(),
                "Reactivated user: " + user.getUsername());

        return userMapper.toReadDto(saved);
    }

    /**
     * Admin hard-delete. Removes the user row entirely.
     */
    @Transactional
    public void adminHardDeleteUser(@NotNull String adminUsername, @NotNull Long userId) {
        AppUser user = findUserOrThrow(userId);

        String username = user.getUsername();

        userRepository.delete(user);

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_USER_HARD_DELETE, ENTITY_NAME, userId.toString(),
                "Hard-deleted user: " + username);
    }

    /** Bulk soft-delete + session revoke. Skips ids that no longer resolve to a user, logs anything else. */
    @Transactional
    public void adminBatchSoftDelete(@NotNull String adminUsername, @NotNull List<Long> ids) {
        for (Long id : ids) {
            try {
                AppUser user = findUserOrThrow(id);
                tombstone(user);
            } catch (ResourceNotFoundException e) {
                log.debug("Skipping soft-delete for missing user id {}", id);
            } catch (Exception e) {
                log.error("Failed to soft-delete user id {}", id, e);
            }
        }
        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_BATCH_SOFT_DELETE, ENTITY_NAME, ids.toString());
    }

    /** Bulk reactivation. Skips users whose original identifiers are now held by an active row. */
    @Transactional
    public void adminBatchReactivate(@NotNull String adminUsername, @NotNull List<Long> ids) {
        for (Long id : ids) {
            try {
                AppUser user = findUserOrThrow(id);
                if (isTombstoned(user)) {
                    assertIdentifiersStillFree(user);
                }
                restore(user);
                userRepository.save(user);
            } catch (ResourceNotFoundException e) {
                log.debug("Skipping reactivate for missing user id {}", id);
            } catch (ResourceAlreadyExistsException e) {
                log.warn("Skipping reactivate for user id {}: {}", id, e.getMessage());
            } catch (Exception e) {
                log.error("Failed to reactivate user id {}", id, e);
            }
        }
        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_BATCH_REACTIVATE, ENTITY_NAME, ids.toString());
    }

    /** Bulk hard-delete. Token cleanup is via DB cascade. */
    @Transactional
    public void adminBatchHardDelete(@NotNull String adminUsername, @NotNull List<Long> ids) {
        for (Long id : ids) {
            try {
                AppUser user = findUserOrThrow(id);
                userRepository.delete(user);
            } catch (ResourceNotFoundException e) {
                log.debug("Skipping hard-delete for missing user id {}", id);
            } catch (Exception e) {
                log.error("Failed to hard-delete user id {}", id, e);
            }
        }
        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_BATCH_HARD_DELETE, ENTITY_NAME, ids.toString());
    }

    /**
     * Stamps the user as tombstoned: disabled, with a {@code deletedAt} timestamp so the
     * partial unique index releases the username/email for re-registration. Also revokes
     * every active refresh token so the disabled account can't continue refreshing.
     */
    private void tombstone(AppUser user) {
        user.setEnabled(false);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user.getId());
    }

    /** Clears the tombstone and re-enables the user. */
    private void restore(AppUser user) {
        user.setEnabled(true);
        user.setDeletedAt(null);
    }

    private boolean isTombstoned(AppUser user) {
        return user.getDeletedAt() != null;
    }

    /**
     * Reactivating a tombstoned user is only safe if the original username and email aren't
     * already held by a currently-active row, otherwise the partial unique index would
     * reject the save with a constraint violation.
     */
    private void assertIdentifiersStillFree(AppUser user) {
        if (userRepository.existsByUsernameAndDeletedAtIsNull(user.getUsername())) {
            throw new ResourceAlreadyExistsException(
                    "Cannot reactivate: username '" + user.getUsername() + "' is now held by another user");
        }
        if (userRepository.existsByEmailAndDeletedAtIsNull(user.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "Cannot reactivate: email '" + user.getEmail() + "' is now held by another user");
        }
    }

    private AppUser findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with id '%s' not found", userId)));
    }

    private AppUser findUserOrThrowByUsername(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with username '%s' not found", username)));
    }

    private void validateUsernameUniqueness(String newUsername, AppUser currentUser) {
        if (newUsername != null
                && !newUsername.equalsIgnoreCase(currentUser.getUsername())
                && userRepository.existsByUsernameAndDeletedAtIsNull(newUsername)) {
            throw new ResourceAlreadyExistsException("Username is already taken");
        }
    }

    private void validateEmailUniqueness(String newEmail, AppUser currentUser) {
        if (newEmail != null
                && !newEmail.equalsIgnoreCase(currentUser.getEmail())
                && userRepository.existsByEmailAndDeletedAtIsNull(newEmail)) {
            throw new ResourceAlreadyExistsException("Email is already registered");
        }
    }
}
