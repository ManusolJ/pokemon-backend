package com.poketeambuilder.services.command;

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
    public UserReadDto updateProfile(@NotNull Long userId, @Valid @NotNull UserUpdateDto dto) {
        AppUser user = findUserOrThrow(userId);

        validateEmailUniqueness(dto.getNewEmail(), user);
        validateUsernameUniqueness(dto.getNewUsername(), user);

        userMapper.updateEntity(dto, user);

        AppUser saved = userRepository.save(user);

        auditLogCommandService.log(saved.getUsername(), AuditAction.USER_PROFILE_UPDATE, ENTITY_NAME, userId.toString());

        return userMapper.toReadDto(saved);
    }

    /**
     * Self-service password change. Validates the current password, persists the new hash,
     * and revokes every active refresh token so old sessions die immediately.
     */
    @Transactional
    public void changePassword(@NotNull Long userId, @Valid @NotNull PasswordChangeDto dto) {
        AppUser user = findUserOrThrow(userId);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BadPasswordException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        userRepository.save(user);

        refreshTokenService.revokeAllForUser(userId);

        auditLogCommandService.log(user.getUsername(), AuditAction.USER_PASSWORD_CHANGE, ENTITY_NAME, userId.toString());
    }

    /** Self-service account disable. Soft-deletes (enabled = false) and revokes every active session. */
    @Transactional
    public void softDeleteAccount(@NotNull Long userId) {
        AppUser user = findUserOrThrow(userId);

        user.setEnabled(false);

        userRepository.save(user);

        refreshTokenService.revokeAllForUser(userId);

        auditLogCommandService.log(user.getUsername(), AuditAction.USER_SELF_DELETE, ENTITY_NAME, userId.toString());
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

    /** Admin soft-delete. Disables the user and revokes every active session. */
    @Transactional
    public void adminSoftDeleteUser(@NotNull String adminUsername, @NotNull Long userId) {
        AppUser user = findUserOrThrow(userId);

        user.setEnabled(false);

        userRepository.save(user);

        refreshTokenService.revokeAllForUser(userId);

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_USER_SOFT_DELETE, ENTITY_NAME, userId.toString(),
                "Soft-deleted user: " + user.getUsername());
    }

    /** Admin reactivation. Only flips the enabled flag — does not restore prior sessions (the user must log in again). */
    @Transactional
    public UserReadDto adminReactivateUser(@NotNull String adminUsername, @NotNull Long userId) {
        AppUser user = findUserOrThrow(userId);

        user.setEnabled(true);

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
                user.setEnabled(false);
                userRepository.save(user);
                refreshTokenService.revokeAllForUser(id);
            } catch (ResourceNotFoundException e) {
                log.debug("Skipping soft-delete for missing user id {}", id);
            } catch (Exception e) {
                log.error("Failed to soft-delete user id {}", id, e);
            }
        }
        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_BATCH_SOFT_DELETE, ENTITY_NAME, ids.toString());
    }

    /** Bulk reactivation. */
    @Transactional
    public void adminBatchReactivate(@NotNull String adminUsername, @NotNull List<Long> ids) {
        for (Long id : ids) {
            try {
                AppUser user = findUserOrThrow(id);
                user.setEnabled(true);
                userRepository.save(user);
            } catch (ResourceNotFoundException e) {
                log.debug("Skipping reactivate for missing user id {}", id);
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

    private AppUser findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with id '%s' not found", userId)));
    }

    private void validateUsernameUniqueness(String newUsername, AppUser currentUser) {
        if (newUsername != null
                && !newUsername.equalsIgnoreCase(currentUser.getUsername())
                && userRepository.existsByUsername(newUsername)) {
            throw new ResourceAlreadyExistsException("Username is already taken");
        }
    }

    private void validateEmailUniqueness(String newEmail, AppUser currentUser) {
        if (newEmail != null
                && !newEmail.equalsIgnoreCase(currentUser.getEmail())
                && userRepository.existsByEmail(newEmail)) {
            throw new ResourceAlreadyExistsException("Email is already registered");
        }
    }
}
