package com.poketeambuilder.integration;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

import javax.sql.DataSource;

import com.poketeambuilder.configuration.JwtProperties;

import com.poketeambuilder.dtos.auth.LoginDto;
import com.poketeambuilder.dtos.auth.TokenResponseDto;
import com.poketeambuilder.dtos.auth.RefreshTokenRequestDto;

import com.poketeambuilder.entities.AppUser;

import com.poketeambuilder.infrastructure.exceptions.InvalidTokenException;

import com.poketeambuilder.mappers.implementation.UserMapper;

import com.poketeambuilder.repositories.UserRepository;

import com.poketeambuilder.services.auth.JwtService;
import com.poketeambuilder.services.auth.AuthService;
import com.poketeambuilder.services.auth.RefreshTokenService;
import com.poketeambuilder.services.auth.CustomUserDetailsService;
import com.poketeambuilder.services.command.AuditLogCommandService;

import com.poketeambuilder.utils.enums.AuditAction;
import com.poketeambuilder.utils.enums.UserRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.Import;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Refresh-token rotation against a real database, with real commits.
 *
 * <p>The unit tests can only show that the revoking calls are made. What matters is what is
 * still in the table afterwards: a revocation followed by a thrown exception inside one
 * transaction is rolled back, and a check-then-update on a row two requests share is a race.
 * Neither shows up with mocks. Each service call here runs and commits in its own transaction
 * ({@code NOT_SUPPORTED} switches off the per-test rollback), and the rows are checked with
 * plain SQL afterwards.</p>
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({
    AuthService.class,
    JwtService.class,
    RefreshTokenService.class,
    AuditLogCommandService.class,
    CustomUserDetailsService.class,
})
@EnableConfigurationProperties(JwtProperties.class)
class RefreshTokenRotationIntegrationTest extends PostgresTestBase {

    private static final String USERNAME = "rotation-ash";

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private DataSource dataSource;

    @MockitoBean private UserMapper userMapper;
    @MockitoBean private PasswordEncoder passwordEncoder;
    @MockitoBean private AuthenticationManager authenticationManager;

    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
        userRepository.save(AppUser.builder()
                .username(USERNAME)
                .email(USERNAME + "@pallet.test")
                .password("hash")
                .role(UserRole.USER)
                .enabled(true)
                .build());
    }

    @AfterEach
    void tearDown() {
        jdbc.update("DELETE FROM audit_log WHERE username = ?", USERNAME);
        jdbc.update("DELETE FROM app_user WHERE username = ?", USERNAME);
    }

    @Test
    @DisplayName("Replaying a spent token leaves every token in its family revoked in the database")
    void replayRevocationIsCommitted() {
        String original = authService.login(login()).refreshToken();
        TokenResponseDto rotated = authService.refresh(new RefreshTokenRequestDto(original));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequestDto(original)))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("reuse");

        assertThat(liveTokens()).isZero();
        assertThat(reuseAuditEntries()).isEqualTo(1);

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequestDto(rotated.refreshToken())))
                .as("the token the thief rotated into must be dead too")
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("Two refreshes racing on one token: one rotates, the other is treated as reuse")
    void concurrentRefreshSpendsTheTokenOnce() throws Exception {
        String original = authService.login(login()).refreshToken();

        List<Object> outcomes = runTogether(
                () -> authService.refresh(new RefreshTokenRequestDto(original)),
                () -> authService.refresh(new RefreshTokenRequestDto(original)));

        assertThat(outcomes).filteredOn(TokenResponseDto.class::isInstance).hasSize(1);
        assertThat(outcomes).filteredOn(InvalidTokenException.class::isInstance).hasSize(1);
        assertThat(reuseAuditEntries()).isEqualTo(1);
        assertThat(liveTokens()).as("reuse ends the whole family, the winner's new token included").isZero();
    }

    @Test
    @DisplayName("Two sign-ins in the same instant get distinct refresh tokens instead of a unique-key clash")
    void simultaneousSignInsDoNotCollide() {
        String first = authService.login(login()).refreshToken();
        String second = authService.login(login()).refreshToken();

        assertThat(first).isNotEqualTo(second);
        assertThat(liveTokens()).isEqualTo(2);
    }

    // --- helpers ---------------------------------------------------------------------------

    private LoginDto login() {
        LoginDto dto = new LoginDto();
        ReflectionTestUtils.setField(dto, "identifier", USERNAME);
        ReflectionTestUtils.setField(dto, "password", "secret");
        return dto;
    }

    private long liveTokens() {
        return jdbc.queryForObject("""
                SELECT count(*) FROM refresh_token rt
                JOIN app_user u ON u.id = rt.user_id
                WHERE u.username = ? AND rt.revoked = false
                """, Long.class, USERNAME);
    }

    private long reuseAuditEntries() {
        return jdbc.queryForObject("SELECT count(*) FROM audit_log WHERE username = ? AND action = ?",
                Long.class, USERNAME, AuditAction.SECURITY_REFRESH_TOKEN_REUSE_DETECTED.getValue());
    }

    /** Starts both calls at the same moment and returns each one's result or exception. */
    @SafeVarargs
    private static List<Object> runTogether(Callable<Object>... calls) throws InterruptedException {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(calls.length);

        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (Callable<Object> call : calls) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return call.call();
                }));
            }
            start.countDown();

            List<Object> outcomes = new ArrayList<>();
            for (Future<Object> future : futures) {
                try {
                    outcomes.add(future.get());
                } catch (ExecutionException e) {
                    outcomes.add(e.getCause());
                }
            }
            return outcomes;
        } finally {
            pool.shutdownNow();
        }
    }
}
