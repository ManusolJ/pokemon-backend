package com.poketeambuilder.services.auth;

import java.util.UUID;
import java.util.Optional;
import java.time.Instant;

import com.poketeambuilder.dtos.auth.LoginDto;
import com.poketeambuilder.dtos.auth.RegisterDto;
import com.poketeambuilder.dtos.auth.TokenResponseDto;
import com.poketeambuilder.dtos.auth.RefreshTokenRequestDto;

import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.RefreshToken;

import com.poketeambuilder.infrastructure.exceptions.InvalidTokenException;
import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;

import com.poketeambuilder.mappers.implementation.UserMapper;

import com.poketeambuilder.repositories.UserRepository;

import com.poketeambuilder.services.command.AuditLogCommandService;

import com.poketeambuilder.utils.enums.AuditAction;
import com.poketeambuilder.utils.enums.UserRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Registration, sign-in and the refresh-rotation lifecycle.
 *
 * <p>Two properties here are security invariants rather than features. Sign-in must fail the
 * same way for an unknown account as for a wrong password, or the endpoint becomes an account
 * enumeration oracle. And presenting an already-spent refresh token has to be treated as a
 * leak, which means revoking the whole family rather than just refusing the one token.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String USERNAME = "ash";
    private static final String EMAIL = "ash@pallet.town";
    private static final long ACCESS_TTL_MS = 900_000L;
    private static final long REFRESH_TTL_MS = 604_800_000L;

    @Mock private JwtService jwtService;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserRepository appUserRepository;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private AuditLogCommandService auditLogCommandService;
    @Mock private CustomUserDetailsService customUserDetailsService;

    @InjectMocks private AuthService authService;

    private AppUser user;

    @BeforeEach
    void setUp() {
        lenient().when(jwtService.getAccessTokenExpirationMs()).thenReturn(ACCESS_TTL_MS);
        lenient().when(jwtService.getRefreshTokenExpirationMs()).thenReturn(REFRESH_TTL_MS);

        user = AppUser.builder()
                .id(1L)
                .username(USERNAME)
                .email(EMAIL)
                .password("hashed")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        UserDetails principal = User.withUsername(USERNAME).password("").authorities("ROLE_USER").build();

        lenient().when(customUserDetailsService.loadUserByUsername(USERNAME)).thenReturn(principal);
        lenient().when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        lenient().when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
    }

    // --- sign-in -----------------------------------------------------------------------

    @Test
    @DisplayName("An unknown identifier fails as bad credentials, not as a missing resource")
    void unknownIdentifierIsIndistinguishableFromWrongPassword() {
        when(appUserRepository.findByUsernameAndDeletedAtIsNull("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(login("nobody", "whatever")))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("An unknown e-mail fails the same way, and doesn't echo the address back")
    void unknownEmailLeaksNothing() {
        when(appUserRepository.findByEmailAndDeletedAtIsNull("ghost@nowhere.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(login("ghost@nowhere.test", "whatever")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageNotContaining("ghost@nowhere.test");
    }

    @Test
    @DisplayName("An identifier containing @ is resolved as an e-mail")
    void resolvesEmailIdentifiers() {
        when(appUserRepository.findByEmailAndDeletedAtIsNull(EMAIL)).thenReturn(Optional.of(user));

        authService.login(login(EMAIL, "secret"));

        verify(appUserRepository).findByEmailAndDeletedAtIsNull(EMAIL);
        verify(appUserRepository, never()).findByUsernameAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("Signing in issues a token pair and opens a new rotation family")
    void loginIssuesTokenPairUnderAFreshFamily() {
        when(appUserRepository.findByUsernameAndDeletedAtIsNull(USERNAME)).thenReturn(Optional.of(user));

        TokenResponseDto response = authService.login(login(USERNAME, "secret"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.expiresIn()).isEqualTo(ACCESS_TTL_MS);

        verify(authenticationManager).authenticate(any());
        verify(refreshTokenService).create(eq(user), eq("refresh-token"), any(UUID.class), any(Instant.class));
    }

    // --- registration ------------------------------------------------------------------

    @Test
    @DisplayName("A taken username is refused before anything is written")
    void refusesTakenUsername() {
        when(appUserRepository.existsByUsernameAndDeletedAtIsNull(USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(register()))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(appUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("A taken e-mail is refused before anything is written")
    void refusesTakenEmail() {
        when(appUserRepository.existsByUsernameAndDeletedAtIsNull(USERNAME)).thenReturn(false);
        when(appUserRepository.existsByEmailAndDeletedAtIsNull(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(register()))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(appUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registration stores the password hashed, never the submitted value")
    void registrationHashesThePassword() {
        when(appUserRepository.existsByUsernameAndDeletedAtIsNull(USERNAME)).thenReturn(false);
        when(appUserRepository.existsByEmailAndDeletedAtIsNull(EMAIL)).thenReturn(false);
        when(userMapper.toEntity(any(RegisterDto.class))).thenReturn(user);
        when(passwordEncoder.encode("plaintext")).thenReturn("hashed-by-encoder");

        authService.register(register());

        assertThat(user.getPassword()).isEqualTo("hashed-by-encoder");
        verify(appUserRepository).save(user);
    }

    // --- refresh rotation --------------------------------------------------------------

    @Test
    @DisplayName("Rotating issues a new pair inside the same family and spends the old token")
    void refreshRotatesWithinTheSameFamily() {
        UUID family = UUID.randomUUID();
        RefreshToken stored = storedToken(family, false, Instant.now().plusSeconds(3600));

        when(refreshTokenService.findByRawToken("raw")).thenReturn(stored);

        authService.refresh(new RefreshTokenRequestDto("raw"));

        verify(refreshTokenService).revoke(stored);
        verify(refreshTokenService).create(eq(user), eq("refresh-token"), eq(family), any(Instant.class));
        verify(refreshTokenService, never()).revokeFamily(any());
    }

    @Test
    @DisplayName("Replaying a spent token revokes the whole family and records it")
    void replayRevokesTheEntireFamily() {
        UUID family = UUID.randomUUID();
        RefreshToken spent = storedToken(family, true, Instant.now().plusSeconds(3600));

        when(refreshTokenService.findByRawToken("raw")).thenReturn(spent);

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequestDto("raw")))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenService).revokeFamily(family);
        verify(auditLogCommandService).log(
                eq(USERNAME),
                eq(AuditAction.SECURITY_REFRESH_TOKEN_REUSE_DETECTED),
                eq("RefreshToken"),
                eq(family.toString()),
                any(String.class));
        verify(refreshTokenService, never()).create(any(), any(), any(), any());
    }

    @Test
    @DisplayName("An expired token is refused without issuing anything")
    void expiredTokenIsRefused() {
        RefreshToken expired = storedToken(UUID.randomUUID(), false, Instant.now().minusSeconds(1));

        when(refreshTokenService.findByRawToken("raw")).thenReturn(expired);

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequestDto("raw")))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenService, never()).create(any(), any(), any(), any());
        verify(refreshTokenService, never()).revokeFamily(any());
    }

    @Test
    @DisplayName("A disabled account cannot refresh, and the attempt ends the whole family")
    void refreshRefusesADisabledAccount() {
        UUID family = UUID.randomUUID();
        when(refreshTokenService.findByRawToken("raw"))
                .thenReturn(storedToken(family, false, Instant.now().plusSeconds(3600)));
        when(customUserDetailsService.loadUserByUsername(USERNAME))
                .thenReturn(User.withUsername(USERNAME).password("").authorities("ROLE_USER").disabled(true).build());

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequestDto("raw")))
                .isInstanceOf(DisabledException.class);

        verify(refreshTokenService).revokeFamily(family);
        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).create(any(), any(), any(), any());
    }

    @Test
    @DisplayName("A token whose account no longer resolves is refused, not reported as a server fault")
    void refreshRefusesAVanishedAccount() {
        UUID family = UUID.randomUUID();
        when(refreshTokenService.findByRawToken("raw"))
                .thenReturn(storedToken(family, false, Instant.now().plusSeconds(3600)));
        when(customUserDetailsService.loadUserByUsername(USERNAME))
                .thenThrow(new UsernameNotFoundException("User not found: " + USERNAME));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequestDto("raw")))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenService).revokeFamily(family);
        verify(refreshTokenService, never()).create(any(), any(), any(), any());
    }

    @Test
    @DisplayName("The presented token is only spent once the account has been cleared")
    void refreshChecksTheAccountBeforeSpendingTheToken() {
        UUID family = UUID.randomUUID();
        RefreshToken stored = storedToken(family, false, Instant.now().plusSeconds(3600));
        when(refreshTokenService.findByRawToken("raw")).thenReturn(stored);
        when(customUserDetailsService.loadUserByUsername(USERNAME))
                .thenReturn(User.withUsername(USERNAME).password("").authorities("ROLE_USER").disabled(true).build());

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequestDto("raw")))
                .isInstanceOf(DisabledException.class);

        verify(refreshTokenService, never()).revoke(stored);
    }

    // --- sign-out ----------------------------------------------------------------------

    @Test
    @DisplayName("Signing out revokes every token in the family, not just the one presented")
    void logoutRevokesTheFamily() {
        UUID family = UUID.randomUUID();

        when(refreshTokenService.findByRawToken("raw"))
                .thenReturn(storedToken(family, false, Instant.now().plusSeconds(3600)));

        authService.logout(new RefreshTokenRequestDto("raw"));

        verify(refreshTokenService).revokeFamily(family);
    }

    // --- helpers -----------------------------------------------------------------------

    private LoginDto login(String identifier, String password) {
        LoginDto dto = new LoginDto();
        ReflectionTestUtils.setField(dto, "identifier", identifier);
        ReflectionTestUtils.setField(dto, "password", password);
        return dto;
    }

    private RegisterDto register() {
        RegisterDto dto = new RegisterDto();
        ReflectionTestUtils.setField(dto, "username", USERNAME);
        ReflectionTestUtils.setField(dto, "email", EMAIL);
        ReflectionTestUtils.setField(dto, "password", "plaintext");
        ReflectionTestUtils.setField(dto, "confirmPassword", "plaintext");
        return dto;
    }

    private RefreshToken storedToken(UUID familyId, boolean revoked, Instant expiresAt) {
        return RefreshToken.builder()
                .id(10L)
                .tokenHash("hash")
                .user(user)
                .familyId(familyId)
                .revoked(revoked)
                .expiresAt(expiresAt)
                .build();
    }
}
