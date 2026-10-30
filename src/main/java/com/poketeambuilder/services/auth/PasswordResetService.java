package com.poketeambuilder.services.auth;

import java.util.UUID;
import java.time.Duration;
import java.time.Instant;

import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.PasswordResetToken;

import com.poketeambuilder.dtos.auth.PasswordResetConfirmDto;
import com.poketeambuilder.dtos.auth.PasswordResetRequestDto;

import com.poketeambuilder.infrastructure.exceptions.InvalidTokenException;

import com.poketeambuilder.repositories.UserRepository;
import com.poketeambuilder.repositories.PasswordResetTokenRepository;

import com.poketeambuilder.utils.token.TokenHashUtil;
import com.poketeambuilder.utils.identity.IdentifierNormalizer;

import com.poketeambuilder.configuration.PasswordResetProperties;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Password-reset flow: request a one-time token by email, then confirm with the token plus
 * a new password. Reset confirmation revokes every active refresh token for the user so
 * old sessions die immediately.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    /** Per-account cap on reset emails, so the endpoint can't be used to flood someone's inbox. */
    private static final int MAX_REQUESTS_PER_WINDOW = 3;
    private static final Duration REQUEST_WINDOW = Duration.ofMinutes(15);

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final TransactionTemplate transactionTemplate;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetProperties passwordResetProperties;

    /**
     * Issues a reset token to the address if it matches a known user, then hands the email to
     * the async sender. The token save runs in a short transaction so the DB connection isn't
     * held during SMTP.
     *
     * <p>Returns the same way, in about the same time, whether the address is unknown, throttled
     * or accepted, so the endpoint can't be used to learn which addresses are registered.
     * Issuing a token spends any earlier one, so only the newest link works.</p>
     */
    public void requestReset(PasswordResetRequestDto requestDto) {
        PreparedReset prepared = transactionTemplate.execute(status ->
                userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(IdentifierNormalizer.email(requestDto.getEmail()))
                        .filter(this::underRequestCap)
                        .map(this::issueToken)
                        .orElse(null)
        );

        if (prepared == null) {
            return;
        }

        emailService.sendPasswordResetEmail(prepared.email(), prepared.url(),
                passwordResetProperties.tokenExpirationMinutes());
    }

    /**
     * Validates the token, sets the new password, marks the token used, and revokes every
     * active refresh token for the user (so all existing sessions are ended). Marking the token
     * used is a conditional UPDATE, so two concurrent confirmations can't both succeed.
     */
    @Transactional
    public void resetPassword(PasswordResetConfirmDto resetDto) {
        String tokenHash = TokenHashUtil.sha256(resetDto.getToken());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }
        if (passwordResetTokenRepository.markUsedIfUnused(resetToken.getId()) == 0) {
            throw new InvalidTokenException("Reset token has already been used");
        }

        AppUser user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(resetDto.getNewPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.invalidateOutstandingForUser(user.getId());
        refreshTokenService.revokeAllForUser(user.getId());
    }

    /** Scheduled cleanup entry point. Drops expired tokens in bulk. */
    @Transactional
    public void purgeExpired() {
        passwordResetTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }

    private boolean underRequestCap(AppUser user) {
        long recent = passwordResetTokenRepository.countByUserIdAndCreatedAtAfter(user.getId(), Instant.now().minus(REQUEST_WINDOW));
        if (recent >= MAX_REQUESTS_PER_WINDOW) {
            log.info("Password reset request for user id {} throttled ({} in the last {})", user.getId(), recent, REQUEST_WINDOW);
            return false;
        }
        return true;
    }

    /**
     * Spends the user's outstanding tokens, saves a fresh one, and returns the e-mail + URL the
     * caller needs to send. The token travels in the URL fragment, which browsers never send to
     * a server, so it stays out of access logs, proxies and {@code Referer} headers.
     */
    private PreparedReset issueToken(AppUser user) {
        String rawToken = UUID.randomUUID().toString();

        passwordResetTokenRepository.invalidateOutstandingForUser(user.getId());

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .tokenHash(TokenHashUtil.sha256(rawToken))
                .user(user)
                .used(false)
                .expiresAt(Instant.now().plusSeconds(passwordResetProperties.tokenExpirationMinutes() * 60L))
                .build();

        passwordResetTokenRepository.save(resetToken);

        return new PreparedReset(user.getEmail(), passwordResetProperties.baseUrl() + "#token=" + rawToken);
    }

    private record PreparedReset(String email, String url) {}
}
