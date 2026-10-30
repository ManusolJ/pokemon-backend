package com.poketeambuilder.services.command;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

import com.poketeambuilder.entities.AppUser;

import com.poketeambuilder.dtos.front.user.UserReadDto;
import com.poketeambuilder.dtos.front.user.UserUpdateDto;
import com.poketeambuilder.dtos.front.user.PasswordChangeDto;

import com.poketeambuilder.dtos.front.admin.user.AdminUserUpdateDto;
import com.poketeambuilder.dtos.front.admin.user.BatchUserActionResultDto;

import com.poketeambuilder.infrastructure.exceptions.BadPasswordException;
import com.poketeambuilder.infrastructure.exceptions.InvalidOperationException;
import com.poketeambuilder.infrastructure.exceptions.ResourceNotFoundException;
import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;

import com.poketeambuilder.mappers.implementation.UserMapper;

import com.poketeambuilder.repositories.UserRepository;

import com.poketeambuilder.services.auth.EmailChangedEvent;
import com.poketeambuilder.services.auth.RefreshTokenService;

import com.poketeambuilder.utils.enums.AuditAction;
import com.poketeambuilder.utils.enums.UserRole;
import com.poketeambuilder.utils.identity.IdentifierNormalizer;

import org.springframework.context.ApplicationEventPublisher;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final TransactionTemplate transactionTemplate;
    private final RefreshTokenService refreshTokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogCommandService auditLogCommandService;

    /**
     * Self-service profile update. Requires the current password: the email address is where
     * password resets are sent, so changing it on the strength of an access token alone would
     * let anyone holding a leaked token take the account over for good.
     *
     * <p>Email and username are checked for uniqueness before save. An email change ends every
     * session and notifies the old address; a username change also revokes the refresh tokens,
     * since the {@code username} claim on any token already issued still carries the old name.</p>
     */
    @Transactional
    public UserReadDto updateProfile(@NotNull Long userId, @Valid @NotNull UserUpdateDto dto) {
        AppUser user = findActiveUserOrThrow(userId);

        assertCurrentPassword(dto.getCurrentPassword(), user);

        validateEmailUniqueness(dto.getNewEmail(), user);
        validateUsernameUniqueness(dto.getNewUsername(), user);

        String oldEmail = user.getEmail();
        boolean emailChanged = dto.getNewEmail() != null && !IdentifierNormalizer.email(dto.getNewEmail()).equals(oldEmail);
        boolean usernameChanged = dto.getNewUsername() != null && !dto.getNewUsername().equals(user.getUsername());

        userMapper.updateEntity(dto, user);
        user.setEmail(IdentifierNormalizer.email(user.getEmail()));

        AppUser saved = userRepository.save(user);

        if (emailChanged || usernameChanged) {
            refreshTokenService.revokeAllForUser(saved.getId());
        }
        if (emailChanged) {
            eventPublisher.publishEvent(new EmailChangedEvent(saved.getUsername(), oldEmail));
        }

        auditLogCommandService.log(saved.getUsername(), AuditAction.USER_PROFILE_UPDATE, ENTITY_NAME, saved.getId().toString());

        return userMapper.toReadDto(saved);
    }

    /**
     * Self-service password change. Validates the current password, persists the new hash,
     * and revokes every active refresh token so old sessions die immediately.
     */
    @Transactional
    public void changePassword(@NotNull Long userId, @Valid @NotNull PasswordChangeDto dto) {
        AppUser user = findActiveUserOrThrow(userId);

        assertCurrentPassword(dto.getCurrentPassword(), user);

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
    public void softDeleteAccount(@NotNull Long userId) {
        AppUser user = findActiveUserOrThrow(userId);

        tombstone(user);

        auditLogCommandService.log(user.getUsername(), AuditAction.USER_SELF_DELETE, ENTITY_NAME, user.getId().toString());
    }

    /**
     * Admin update of a user's role/email/username/enabled flag. Uniqueness validated as for
     * self-service. Refuses any edit that would drop the last administrator, since recovering
     * from that means editing the database by hand.
     */
    @Transactional
    public UserReadDto adminUpdateUser(@NotNull String adminUsername, @NotNull Long userId, @Valid @NotNull AdminUserUpdateDto dto) {
        AppUser user = findUserOrThrow(userId);

        validateEmailUniqueness(dto.getNewEmail(), user);
        validateUsernameUniqueness(dto.getNewUsername(), user);

        if (demotesAdmin(user, dto) || disablesUser(dto)) {
            assertNotLastAdmin(user);
        }

        boolean securityRelevant = disablesUser(dto) || changesRole(user, dto) || changesUsername(user, dto);

        userMapper.updateEntity(dto, user);
        user.setEmail(IdentifierNormalizer.email(user.getEmail()));

        AppUser saved = userRepository.save(user);

        if (securityRelevant) {
            refreshTokenService.revokeAllForUser(saved.getId());
        }

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_USER_UPDATE, ENTITY_NAME, userId.toString());

        return userMapper.toReadDto(saved);
    }

    /** Admin soft-delete. Tombstones the row and revokes every active session. */
    @Transactional
    public void adminSoftDeleteUser(@NotNull String adminUsername, @NotNull Long userId) {
        AppUser user = findUserOrThrow(userId);

        assertNotLastAdmin(user);

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

        assertNotLastAdmin(user);

        String username = user.getUsername();

        userRepository.delete(user);

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_USER_HARD_DELETE, ENTITY_NAME, userId.toString(),
                "Hard-deleted user: " + username);
    }

    /** Bulk soft-delete + session revoke. Reports what happened to each id. */
    public BatchUserActionResultDto adminBatchSoftDelete(@NotNull String adminUsername, @NotNull List<Long> ids) {
        BatchUserActionResultDto result = forEachInOwnTransaction(ids, "soft-delete", id -> {
            AppUser user = findUserOrThrow(id);
            assertNotLastAdmin(user);
            tombstone(user);
        });
        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_BATCH_SOFT_DELETE, ENTITY_NAME, null, batchDetails(ids, result));
        return result;
    }

    /** Bulk reactivation. Skips users whose original identifiers are now held by an active row. */
    public BatchUserActionResultDto adminBatchReactivate(@NotNull String adminUsername, @NotNull List<Long> ids) {
        BatchUserActionResultDto result = forEachInOwnTransaction(ids, "reactivate", id -> {
            AppUser user = findUserOrThrow(id);
            if (isTombstoned(user)) {
                assertIdentifiersStillFree(user);
            }
            restore(user);
            userRepository.save(user);
        });
        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_BATCH_REACTIVATE, ENTITY_NAME, null, batchDetails(ids, result));
        return result;
    }

    /** Bulk hard-delete. Token cleanup is via DB cascade. */
    public BatchUserActionResultDto adminBatchHardDelete(@NotNull String adminUsername, @NotNull List<Long> ids) {
        BatchUserActionResultDto result = forEachInOwnTransaction(ids, "hard-delete", id -> {
            AppUser user = findUserOrThrow(id);
            assertNotLastAdmin(user);
            userRepository.delete(user);
        });
        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_BATCH_HARD_DELETE, ENTITY_NAME, null, batchDetails(ids, result));
        return result;
    }

    /**
     * Applies {@code action} to each id in its own transaction so one bad row doesn't take the
     * batch with it. Catching per-item inside a single shared transaction wouldn't work:
     * whichever failure marked it rollback-only would still doom the commit at the end, and
     * the loop would have reported success for every id it processed after that point.
     */
    private BatchUserActionResultDto forEachInOwnTransaction(List<Long> ids, String action, Consumer<Long> operation) {
        List<Long> succeeded = new ArrayList<>();
        List<Long> skipped = new ArrayList<>();
        List<Long> failed = new ArrayList<>();

        for (Long id : ids) {
            try {
                transactionTemplate.executeWithoutResult(status -> operation.accept(id));
                succeeded.add(id);
            } catch (ResourceNotFoundException e) {
                log.debug("Skipping {} for missing user id {}", action, id);
                skipped.add(id);
            } catch (ResourceAlreadyExistsException | InvalidOperationException e) {
                log.warn("Skipping {} for user id {}: {}", action, id, e.getMessage());
                skipped.add(id);
            } catch (Exception e) {
                log.error("Failed to {} user id {}", action, id, e);
                failed.add(id);
            }
        }

        return new BatchUserActionResultDto(List.copyOf(succeeded), List.copyOf(skipped), List.copyOf(failed));
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

    private void assertCurrentPassword(String currentPassword, AppUser user) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadPasswordException("Current password is incorrect");
        }
    }

    /**
     * Blocks the change if {@code user} is the only administrator left who can still sign in.
     * There is no in-app way back from an instance with no admins - the README's first-run
     * {@code UPDATE app_user SET role = 'ADMIN'} is the only recovery.
     *
     * <p>The count is taken under a row lock on every active admin, so two admins demoting or
     * removing each other at the same moment are serialised instead of both seeing a second
     * admin and both succeeding.</p>
     */
    private void assertNotLastAdmin(AppUser user) {
        if (!UserRole.ADMIN.equals(user.getRole()) || isTombstoned(user) || !user.getEnabled()) {
            return;
        }

        if (userRepository.lockActiveByRole(UserRole.ADMIN).size() <= 1) {
            throw new InvalidOperationException(
                    "Cannot remove the last administrator; promote another account first");
        }
    }

    private boolean demotesAdmin(AppUser user, AdminUserUpdateDto dto) {
        return UserRole.ADMIN.equals(user.getRole())
                && dto.getNewRole() != null
                && !UserRole.ADMIN.name().equals(dto.getNewRole());
    }

    private boolean disablesUser(AdminUserUpdateDto dto) {
        return Boolean.FALSE.equals(dto.getEnabled());
    }

    /** Any role change, in either direction. Must be evaluated before the mapper applies the update. */
    private boolean changesRole(AppUser user, AdminUserUpdateDto dto) {
        return dto.getNewRole() != null && !user.getRole().name().equals(dto.getNewRole());
    }

    /** A username change leaves every issued token carrying the old name in its {@code username} claim. */
    private boolean changesUsername(AppUser user, AdminUserUpdateDto dto) {
        return dto.getNewUsername() != null && !dto.getNewUsername().equals(user.getUsername());
    }

    /**
     * Renders a batch's target ids and outcome for the audit trail. The list goes in
     * {@code details} ({@code TEXT}) rather than {@code entityId}, which is a 50-character
     * column that a hundred-id batch overruns many times over - and because each item commits
     * in its own transaction, a failed audit write left the accounts deleted with no record of it.
     */
    private String batchDetails(List<Long> ids, BatchUserActionResultDto result) {
        return String.format("%d user id(s) requested: %s", ids.size(), result);
    }

    /**
     * Reactivating a tombstoned user is only safe if the original username and email aren't
     * already held by a currently-active row, otherwise the partial unique index would
     * reject the save with a constraint violation.
     */
    private void assertIdentifiersStillFree(AppUser user) {
        if (userRepository.existsByUsernameIgnoreCaseAndDeletedAtIsNull(user.getUsername())) {
            throw new ResourceAlreadyExistsException(
                    "Cannot reactivate: username '" + user.getUsername() + "' is now held by another user");
        }
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(user.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "Cannot reactivate: email '" + user.getEmail() + "' is now held by another user");
        }
    }

    private AppUser findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with id '%s' not found", userId)));
    }

    private AppUser findActiveUserOrThrow(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with id '%s' not found", userId)));
    }

    /**
     * A change to the user's own name in a different case ({@code ash} to {@code Ash}) is not a
     * clash with themselves; any other match against an active row is.
     */
    private void validateUsernameUniqueness(String newUsername, AppUser currentUser) {
        if (newUsername != null
                && !newUsername.equalsIgnoreCase(currentUser.getUsername())
                && userRepository.existsByUsernameIgnoreCaseAndDeletedAtIsNull(newUsername)) {
            throw new ResourceAlreadyExistsException("Username is already taken");
        }
    }

    private void validateEmailUniqueness(String newEmail, AppUser currentUser) {
        if (newEmail != null
                && !newEmail.equalsIgnoreCase(currentUser.getEmail())
                && userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(newEmail)) {
            throw new ResourceAlreadyExistsException("Email is already registered");
        }
    }
}
