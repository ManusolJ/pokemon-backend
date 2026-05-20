package com.poketeambuilder.services.command;

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

import com.poketeambuilder.utils.enums.AuditAction;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.validation.annotation.Validated;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class UserCommandService {

    private static final String ENTITY_NAME = "User";

    private final UserMapper userMapper;
    private final UserRepository UserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogCommandService auditLogCommandService;

    @Transactional
    public UserReadDto updateProfile(@NotNull Long userId, @Valid @NotNull UserUpdateDto dto) {
        AppUser user = findUserOrThrow(userId);

        validateEmailUniqueness(dto.getNewEmail(), user);
        validateUsernameUniqueness(dto.getNewUsername(), user);

        userMapper.updateEntity(dto, user);

        AppUser saved = UserRepository.save(user);

        auditLogCommandService.log(saved.getUsername(), AuditAction.USER_PROFILE_UPDATE, ENTITY_NAME, userId.toString());

        return userMapper.toReadDto(saved);
    }

    @Transactional
    public void changePassword(@NotNull Long userId, @Valid @NotNull PasswordChangeDto dto) {
        AppUser user = findUserOrThrow(userId);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BadPasswordException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        UserRepository.save(user);

        auditLogCommandService.log(user.getUsername(), AuditAction.USER_PASSWORD_CHANGE, ENTITY_NAME, userId.toString());
    }

    @Transactional
    public void softDeleteAccount(@NotNull Long userId) {
        AppUser user = findUserOrThrow(userId);

        user.setEnabled(false);

        UserRepository.save(user);

        auditLogCommandService.log(user.getUsername(), AuditAction.USER_SELF_DELETE, ENTITY_NAME, userId.toString());
    }

    @Transactional
    public UserReadDto adminUpdateUser(@NotNull String adminUsername, @NotNull Long userId, @Valid @NotNull AdminUserUpdateDto dto) {
        AppUser user = findUserOrThrow(userId);

        validateEmailUniqueness(dto.getNewEmail(), user);
        validateUsernameUniqueness(dto.getNewUsername(), user);

        userMapper.updateEntity(dto, user);

        AppUser saved = UserRepository.save(user);

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_USER_UPDATE, ENTITY_NAME, userId.toString());

        return userMapper.toReadDto(saved);
    }

    @Transactional
    public void adminSoftDeleteUser(@NotNull String adminUsername, @NotNull Long userId) {
        AppUser user = findUserOrThrow(userId);

        user.setEnabled(false);

        UserRepository.save(user);

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_USER_SOFT_DELETE, ENTITY_NAME, userId.toString(),
                "Soft-deleted user: " + user.getUsername());
    }

    @Transactional
    public UserReadDto adminReactivateUser(@NotNull String adminUsername, @NotNull Long userId) {
        AppUser user = findUserOrThrow(userId);

        user.setEnabled(true);

        AppUser saved = UserRepository.save(user);

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_USER_REACTIVATE, ENTITY_NAME, userId.toString(),
                "Reactivated user: " + user.getUsername());

        return userMapper.toReadDto(saved);
    }

    @Transactional
    public void adminHardDeleteUser(@NotNull String adminUsername, @NotNull Long userId) {
        AppUser user = findUserOrThrow(userId);

        String username = user.getUsername();

        UserRepository.delete(user);

        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_USER_HARD_DELETE, ENTITY_NAME, userId.toString(),
                "Hard-deleted user: " + username);
    }

    @Transactional
    public void adminBatchSoftDelete(@NotNull String adminUsername, @NotNull List<Long> ids) {
        for (Long id : ids) {
            try {
                AppUser user = findUserOrThrow(id);
                user.setEnabled(false);
                UserRepository.save(user);
            } catch (Exception ignored) { /* skip missing/already-deleted */ }
        }
        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_BATCH_SOFT_DELETE, ENTITY_NAME, ids.toString());
    }

    @Transactional
    public void adminBatchReactivate(@NotNull String adminUsername, @NotNull List<Long> ids) {
        for (Long id : ids) {
            try {
                AppUser user = findUserOrThrow(id);
                user.setEnabled(true);
                UserRepository.save(user);
            } catch (Exception ignored) { /* skip missing */ }
        }
        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_BATCH_REACTIVATE, ENTITY_NAME, ids.toString());
    }

    @Transactional
    public void adminBatchHardDelete(@NotNull String adminUsername, @NotNull List<Long> ids) {
        for (Long id : ids) {
            try {
                AppUser user = findUserOrThrow(id);
                UserRepository.delete(user);
            } catch (Exception ignored) { /* skip missing */ }
        }
        auditLogCommandService.log(adminUsername, AuditAction.ADMIN_BATCH_HARD_DELETE, ENTITY_NAME, ids.toString());
    }

    private AppUser findUserOrThrow(Long userId) {
        return UserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with id '%s' not found", userId)));
    }

    private void validateUsernameUniqueness(String newUsername, AppUser currentUser) {
        if (newUsername != null
                && !newUsername.equalsIgnoreCase(currentUser.getUsername())
                && UserRepository.existsByUsername(newUsername)) {
            throw new ResourceAlreadyExistsException("Username is already taken");
        }
    }

    private void validateEmailUniqueness(String newEmail, AppUser currentUser) {
        if (newEmail != null
                && !newEmail.equalsIgnoreCase(currentUser.getEmail())
                && UserRepository.existsByEmail(newEmail)) {
            throw new ResourceAlreadyExistsException("Email is already registered");
        }
    }
}