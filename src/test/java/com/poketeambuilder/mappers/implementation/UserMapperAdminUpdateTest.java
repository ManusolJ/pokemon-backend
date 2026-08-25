package com.poketeambuilder.mappers.implementation;

import com.poketeambuilder.dtos.front.admin.user.AdminUserUpdateDto;

import com.poketeambuilder.entities.AppUser;

import com.poketeambuilder.utils.enums.UserRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The admin user mutation, against the mapper MapStruct actually generates.
 *
 * <p>{@code @BeanMapping(ignoreByDefault = true)} means an unlisted target property is dropped in
 * silence rather than failing the build. {@code enabled} was unlisted, so disabling an account
 * through the admin edit returned 200 and changed nothing. A service-level test cannot catch that
 * — it stubs the mapper — so the assertion has to run against the generated implementation.</p>
 */
class UserMapperAdminUpdateTest {

    private final UserMapper mapper = new UserMapperImpl();

    @Test
    @DisplayName("Disabling an account is applied to the entity")
    void appliesDisable() {
        AppUser user = user();

        mapper.updateEntity(adminUpdate(null, false, null, null), user);

        assertThat(user.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("Re-enabling an account is applied too")
    void appliesEnable() {
        AppUser user = user();
        user.setEnabled(false);

        mapper.updateEntity(adminUpdate(null, true, null, null), user);

        assertThat(user.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("An omitted enabled flag leaves the current value alone")
    void leavesEnabledAloneWhenAbsent() {
        AppUser user = user();

        mapper.updateEntity(adminUpdate("ADMIN", null, null, null), user);

        assertThat(user.getEnabled()).isTrue();
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Every field the payload exposes reaches the entity")
    void appliesEveryField() {
        AppUser user = user();

        mapper.updateEntity(adminUpdate("ADMIN", false, "new@address.test", "misty"), user);

        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(user.getEnabled()).isFalse();
        assertThat(user.getEmail()).isEqualTo("new@address.test");
        assertThat(user.getUsername()).isEqualTo("misty");
    }

    @Test
    @DisplayName("An empty payload is a no-op rather than a wipe")
    void emptyPayloadChangesNothing() {
        AppUser user = user();

        mapper.updateEntity(adminUpdate(null, null, null, null), user);

        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getEnabled()).isTrue();
        assertThat(user.getEmail()).isEqualTo("ash@test.local");
        assertThat(user.getUsername()).isEqualTo("ash");
    }

    private AppUser user() {
        return AppUser.builder()
                .id(1L)
                .username("ash")
                .email("ash@test.local")
                .password("hashed")
                .role(UserRole.USER)
                .enabled(true)
                .build();
    }

    private AdminUserUpdateDto adminUpdate(String newRole, Boolean enabled, String newEmail, String newUsername) {
        AdminUserUpdateDto dto = new AdminUserUpdateDto();
        ReflectionTestUtils.setField(dto, "newRole", newRole);
        ReflectionTestUtils.setField(dto, "enabled", enabled);
        ReflectionTestUtils.setField(dto, "newEmail", newEmail);
        ReflectionTestUtils.setField(dto, "newUsername", newUsername);
        return dto;
    }
}
