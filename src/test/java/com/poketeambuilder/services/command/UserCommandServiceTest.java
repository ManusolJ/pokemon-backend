package com.poketeambuilder.services.command;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.poketeambuilder.dtos.front.user.UserUpdateDto;
import com.poketeambuilder.dtos.front.user.PasswordChangeDto;
import com.poketeambuilder.dtos.front.admin.user.AdminUserUpdateDto;

import com.poketeambuilder.entities.AppUser;

import com.poketeambuilder.infrastructure.exceptions.BadPasswordException;
import com.poketeambuilder.infrastructure.exceptions.InvalidOperationException;

import com.poketeambuilder.mappers.implementation.UserMapper;

import com.poketeambuilder.repositories.UserRepository;

import com.poketeambuilder.services.auth.RefreshTokenService;

import com.poketeambuilder.utils.enums.AuditAction;
import com.poketeambuilder.utils.enums.UserRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Account mutations, with the emphasis on the two rules that protect the instance from itself:
 * an administrator must not be able to remove the last account that can reach the admin panel,
 * and anything that changes credentials has to end the sessions carrying the old ones.
 */
@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    private static final long ADMIN_ID = 1L;
    private static final long USER_ID = 2L;

    @Mock private UserMapper userMapper;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuditLogCommandService auditLogCommandService;

    @InjectMocks private UserCommandService userCommandService;

    @Captor private ArgumentCaptor<String> auditDetails;

    private AppUser admin;
    private AppUser regularUser;

    @BeforeEach
    void setUp() {
        admin = user(ADMIN_ID, "oak", UserRole.ADMIN);
        regularUser = user(USER_ID, "ash", UserRole.USER);

        lenient().when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
        lenient().when(userRepository.findById(USER_ID)).thenReturn(Optional.of(regularUser));
        lenient().when(userRepository.findByUsernameAndDeletedAtIsNull("ash")).thenReturn(Optional.of(regularUser));
        lenient().when(userRepository.save(any(AppUser.class))).thenAnswer(call -> call.getArgument(0));
    }

    // --- last-administrator protection --------------------------------------------------

    @Test
    @DisplayName("The only administrator cannot be soft-deleted")
    void refusesToTombstoneTheLastAdmin() {
        onlyOneAdminRemains();

        assertThatThrownBy(() -> userCommandService.adminSoftDeleteUser("oak", ADMIN_ID))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("last administrator");

        assertThat(admin.getDeletedAt()).isNull();
        assertThat(admin.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("The only administrator cannot be hard-deleted")
    void refusesToHardDeleteTheLastAdmin() {
        onlyOneAdminRemains();

        assertThatThrownBy(() -> userCommandService.adminHardDeleteUser("oak", ADMIN_ID))
                .isInstanceOf(InvalidOperationException.class);

        verify(userRepository, never()).delete(any(AppUser.class));
    }

    @Test
    @DisplayName("The only administrator cannot be demoted")
    void refusesToDemoteTheLastAdmin() {
        onlyOneAdminRemains();

        assertThatThrownBy(() -> userCommandService.adminUpdateUser("oak", ADMIN_ID, adminUpdate("USER", null)))
                .isInstanceOf(InvalidOperationException.class);

        verify(userMapper, never()).updateEntity(any(AdminUserUpdateDto.class), any());
    }

    @Test
    @DisplayName("The only administrator cannot be disabled")
    void refusesToDisableTheLastAdmin() {
        onlyOneAdminRemains();

        assertThatThrownBy(() -> userCommandService.adminUpdateUser("oak", ADMIN_ID, adminUpdate(null, false)))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    @DisplayName("With a second administrator in place, the first can be removed")
    void allowsRemovalWhenAnotherAdminExists() {
        when(userRepository.countByRoleAndEnabledTrueAndDeletedAtIsNull(UserRole.ADMIN)).thenReturn(2L);

        assertThatCode(() -> userCommandService.adminSoftDeleteUser("oak", ADMIN_ID)).doesNotThrowAnyException();

        assertThat(admin.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("The guard applies to administrators only, never to ordinary accounts")
    void ordinaryAccountsAreUnaffectedByTheGuard() {
        onlyOneAdminRemains();

        assertThatCode(() -> userCommandService.adminSoftDeleteUser("oak", USER_ID)).doesNotThrowAnyException();

        assertThat(regularUser.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("An already-disabled administrator doesn't count towards the last-admin check")
    void disabledAdminIsNotTreatedAsTheLastOne() {
        admin.setEnabled(false);
        onlyOneAdminRemains();

        assertThatCode(() -> userCommandService.adminSoftDeleteUser("oak", ADMIN_ID)).doesNotThrowAnyException();
    }

    // --- credential changes end sessions --------------------------------------------------

    @Test
    @DisplayName("Changing a password revokes every refresh token the account holds")
    void passwordChangeRevokesSessions() {
        when(passwordEncoder.matches("current", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("brand-new")).thenReturn("new-hash");

        userCommandService.changePassword("ash", passwordChange("current", "brand-new"));

        assertThat(regularUser.getPassword()).isEqualTo("new-hash");
        verify(refreshTokenService).revokeAllForUser(USER_ID);
    }

    @Test
    @DisplayName("A wrong current password changes nothing and ends no sessions")
    void wrongCurrentPasswordIsRejected() {
        when(passwordEncoder.matches("guess", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userCommandService.changePassword("ash", passwordChange("guess", "brand-new")))
                .isInstanceOf(BadPasswordException.class);

        assertThat(regularUser.getPassword()).isEqualTo("hashed");
        verify(refreshTokenService, never()).revokeAllForUser(anyLong());
    }

    @Test
    @DisplayName("Renaming an account revokes its sessions, since the token subject no longer resolves")
    void usernameChangeRevokesSessions() {
        userCommandService.updateProfile("ash", profileUpdate("misty", null));

        verify(refreshTokenService).revokeAllForUser(USER_ID);
    }

    @Test
    @DisplayName("Changing only the e-mail leaves sessions alone")
    void emailChangeKeepsSessions() {
        userCommandService.updateProfile("ash", profileUpdate(null, "new@address.test"));

        verify(refreshTokenService, never()).revokeAllForUser(anyLong());
    }

    @Test
    @DisplayName("Submitting the same username is not a rename")
    void unchangedUsernameIsNotARename() {
        userCommandService.updateProfile("ash", profileUpdate("ash", null));

        verify(refreshTokenService, never()).revokeAllForUser(anyLong());
    }

    @Test
    @DisplayName("Tombstoning disables the account, stamps it, and ends its sessions")
    void tombstoneDisablesStampsAndRevokes() {
        userCommandService.softDeleteAccount("ash");

        assertThat(regularUser.getEnabled()).isFalse();
        assertThat(regularUser.getDeletedAt()).isNotNull();
        verify(refreshTokenService).revokeAllForUser(USER_ID);
    }

    // --- admin edits -----------------------------------------------------------------------

    @Test
    @DisplayName("Disabling an account through the admin edit actually clears the enabled flag")
    void adminDisableIsPersisted() {
        applyAdminUpdateForReal();

        userCommandService.adminUpdateUser("oak", USER_ID, adminUpdate(null, false));

        assertThat(regularUser.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("Disabling an account ends its sessions, so the refresh token cannot outlive it")
    void adminDisableRevokesSessions() {
        applyAdminUpdateForReal();

        userCommandService.adminUpdateUser("oak", USER_ID, adminUpdate(null, false));

        verify(refreshTokenService).revokeAllForUser(USER_ID);
    }

    @Test
    @DisplayName("Changing a role ends the sessions still carrying the old one")
    void adminRoleChangeRevokesSessions() {
        applyAdminUpdateForReal();

        userCommandService.adminUpdateUser("oak", USER_ID, adminUpdate("ADMIN", null));

        verify(refreshTokenService).revokeAllForUser(USER_ID);
    }

    @Test
    @DisplayName("An edit that changes nothing security-relevant leaves sessions alone")
    void adminEmailEditKeepsSessions() {
        applyAdminUpdateForReal();

        userCommandService.adminUpdateUser("oak", USER_ID, adminUpdate(null, null));

        verify(refreshTokenService, never()).revokeAllForUser(anyLong());
    }

    // --- batch isolation -------------------------------------------------------------------

    @Test
    @DisplayName("Each id in a batch runs in its own transaction, so one failure doesn't sink the rest")
    void batchIsolatesFailuresPerItem() {
        runTransactionsInline();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        userCommandService.adminBatchSoftDelete("oak", List.of(USER_ID, 99L, USER_ID));

        verify(transactionTemplate, times(3)).executeWithoutResult(any());
        assertThat(regularUser.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("A batch records its ids in details, not in the 50-character entity_id column")
    void batchAuditKeepsIdsOutOfTheReferenceColumn() {
        runTransactionsInline();
        List<Long> ids = java.util.stream.LongStream.rangeClosed(1, 100).boxed().toList();

        userCommandService.adminBatchHardDelete("oak", ids);

        verify(auditLogCommandService).log(eq("oak"), eq(AuditAction.ADMIN_BATCH_HARD_DELETE),
                eq("User"), isNull(), auditDetails.capture());
        assertThat(auditDetails.getValue()).contains("100 user id(s)");
    }

    // --- helpers ---------------------------------------------------------------------------

    /**
     * Makes the mapper mock behave like the real generated one for the fields under test. The
     * service delegates the write to MapStruct, so a no-op mock would let a regression in the
     * mapping pass unnoticed here.
     */
    private void applyAdminUpdateForReal() {
        doAnswer(call -> {
            AdminUserUpdateDto dto = call.getArgument(0);
            AppUser target = call.getArgument(1);
            if (dto.getEnabled() != null) {
                target.setEnabled(dto.getEnabled());
            }
            if (dto.getNewRole() != null) {
                target.setRole(UserRole.valueOf(dto.getNewRole()));
            }
            return null;
        }).when(userMapper).updateEntity(any(AdminUserUpdateDto.class), any(AppUser.class));
    }

    /**
     * States that a single administrator is left. Lenient because two of the callers assert the
     * opposite of the usual case: that the guard returns before it ever counts, so the stub is
     * meant to go unused there.
     */
    private void onlyOneAdminRemains() {
        lenient().when(userRepository.countByRoleAndEnabledTrueAndDeletedAtIsNull(UserRole.ADMIN)).thenReturn(1L);
    }

    @SuppressWarnings("unchecked")
    private void runTransactionsInline() {
        doAnswer(call -> {
            ((Consumer<TransactionStatus>) call.getArgument(0)).accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    private AppUser user(long id, String username, UserRole role) {
        return AppUser.builder()
                .id(id)
                .username(username)
                .email(username + "@test.local")
                .password("hashed")
                .role(role)
                .enabled(true)
                .build();
    }

    private AdminUserUpdateDto adminUpdate(String newRole, Boolean enabled) {
        AdminUserUpdateDto dto = new AdminUserUpdateDto();
        ReflectionTestUtils.setField(dto, "newRole", newRole);
        ReflectionTestUtils.setField(dto, "enabled", enabled);
        return dto;
    }

    private UserUpdateDto profileUpdate(String newUsername, String newEmail) {
        UserUpdateDto dto = new UserUpdateDto();
        ReflectionTestUtils.setField(dto, "newUsername", newUsername);
        ReflectionTestUtils.setField(dto, "newEmail", newEmail);
        return dto;
    }

    private PasswordChangeDto passwordChange(String current, String replacement) {
        PasswordChangeDto dto = new PasswordChangeDto();
        ReflectionTestUtils.setField(dto, "currentPassword", current);
        ReflectionTestUtils.setField(dto, "newPassword", replacement);
        return dto;
    }
}
