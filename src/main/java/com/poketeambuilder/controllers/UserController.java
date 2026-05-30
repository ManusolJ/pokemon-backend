package com.poketeambuilder.controllers;

import com.poketeambuilder.dtos.front.user.UserFilterDto;
import com.poketeambuilder.dtos.front.user.UserReadDto;
import com.poketeambuilder.dtos.front.user.UserSummaryDto;
import com.poketeambuilder.dtos.front.user.UserUpdateDto;
import com.poketeambuilder.dtos.front.user.PasswordChangeDto;

import com.poketeambuilder.dtos.front.admin.user.AdminUserUpdateDto;
import com.poketeambuilder.dtos.front.admin.user.BatchUserActionDto;

import com.poketeambuilder.services.query.UserQueryService;
import com.poketeambuilder.services.command.UserCommandService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

/**
 * User-account REST surface. Splits cleanly into two areas:
 * <ul>
 *   <li>{@code /me/*}: self-service profile reads + updates (authentication required).</li>
 *   <li>{@code /admin/*}: privileged user management, guarded by {@code @PreAuthorize("hasRole('ADMIN')")}
 *       and {@code SecurityConfig}.</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    /** Returns the authenticated user's full profile. */
    @GetMapping("/me")
    public ResponseEntity<UserReadDto> getMe(@AuthenticationPrincipal UserDetails user) {
        UserReadDto profile = userQueryService.findByUsername(user.getUsername());
        return ResponseEntity.ok(profile);
    }

    /** Updates the authenticated user's profile (email/username). */
    @PutMapping("/me")
    public ResponseEntity<UserReadDto> updateMe(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody UserUpdateDto dto) {
        UserReadDto updated = userCommandService.updateProfile(user.getUsername(), dto);
        return ResponseEntity.ok(updated);
    }

    /** Changes the authenticated user's password. Revokes every active session on success. */
    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody PasswordChangeDto dto) {
        userCommandService.changePassword(user.getUsername(), dto);
        return ResponseEntity.noContent().build();
    }

    /** Self-service soft-delete: disables the account and revokes every active session. */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal UserDetails user) {
        userCommandService.softDeleteAccount(user.getUsername());
        return ResponseEntity.noContent().build();
    }

    /** Admin: paged user listing filtered by {@link UserFilterDto}. */
    @PostMapping("/admin/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserReadDto>> getUsers(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody UserFilterDto filter) {
        Page<UserReadDto> users = userQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(users);
    }

    /** Admin: paged lightweight summaries (id + username + email + role + enabled). */
    @PostMapping("/admin/summaries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserSummaryDto>> getUserSummaries(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody UserFilterDto filter) {
        Page<UserSummaryDto> summaries = userQueryService.filterSummaries(filter, pageable);
        return ResponseEntity.ok(summaries);
    }

    /** Admin: single-user fetch by id. */
    @PostMapping("/admin/id")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserReadDto> getUserById(@RequestBody UserFilterDto filter) {
        UserReadDto user = userQueryService.findById(filter.getId());
        return ResponseEntity.ok(user);
    }

    /** Admin: count of users matching a filter. */
    @PostMapping("/admin/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getUserCount(@RequestBody UserFilterDto filter) {
        long count = userQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(count);
    }

    /** Admin: update a user's role/email/username/enabled flag. */
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserReadDto> adminUpdateUser(@AuthenticationPrincipal UserDetails admin, @PathVariable Long id, @Valid @RequestBody AdminUserUpdateDto dto) {
        UserReadDto updated = userCommandService.adminUpdateUser(admin.getUsername(), id, dto);
        return ResponseEntity.ok(updated);
    }

    /** Admin: soft-delete (disable). Revokes every active session for the target user. */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminSoftDeleteUser(@AuthenticationPrincipal UserDetails admin, @PathVariable Long id) {
        userCommandService.adminSoftDeleteUser(admin.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    /** Admin: re-enable a previously-disabled account. */
    @PostMapping("/admin/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserReadDto> adminReactivateUser(@AuthenticationPrincipal UserDetails admin, @PathVariable Long id) {
        UserReadDto reactivated = userCommandService.adminReactivateUser(admin.getUsername(), id);
        return ResponseEntity.ok(reactivated);
    }

    /** Admin: hard-delete. Removes the user row entirely (cascades wipe associated tokens). */
    @DeleteMapping("/admin/{id}/hard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminHardDeleteUser(@AuthenticationPrincipal UserDetails admin, @PathVariable Long id) {
        userCommandService.adminHardDeleteUser(admin.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    /** Admin: bulk soft-delete. Missing ids are skipped, errors are logged. */
    @PostMapping("/admin/batch/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminBatchSoftDelete(@AuthenticationPrincipal UserDetails admin, @Valid @RequestBody BatchUserActionDto dto) {
        userCommandService.adminBatchSoftDelete(admin.getUsername(), dto.getIds());
        return ResponseEntity.noContent().build();
    }

    /** Admin: bulk reactivation. */
    @PostMapping("/admin/batch/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminBatchReactivate(@AuthenticationPrincipal UserDetails admin, @Valid @RequestBody BatchUserActionDto dto) {
        userCommandService.adminBatchReactivate(admin.getUsername(), dto.getIds());
        return ResponseEntity.noContent().build();
    }

    /** Admin: bulk hard-delete. */
    @PostMapping("/admin/batch/hard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminBatchHardDelete(@AuthenticationPrincipal UserDetails admin, @Valid @RequestBody BatchUserActionDto dto) {
        userCommandService.adminBatchHardDelete(admin.getUsername(), dto.getIds());
        return ResponseEntity.noContent().build();
    }
}
