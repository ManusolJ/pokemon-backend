package com.poketeambuilder.services.auth;

import java.util.UUID;
import java.time.Instant;

import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.PasswordResetToken;

import com.poketeambuilder.dtos.auth.PasswordResetConfirmDto;
import com.poketeambuilder.dtos.auth.PasswordResetRequestDto;

import com.poketeambuilder.infrastructure.exceptions.InvalidTokenException;

import com.poketeambuilder.repositories.UserRepository;
import com.poketeambuilder.repositories.PasswordResetTokenRepository;

import com.poketeambuilder.utils.token.TokenHashUtil;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;

/**
 * Password-reset flow: request a one-time token by email, then confirm with the token plus
 * a new password. Reset confirmation revokes every active refresh token for the user so
 * old sessions die immediately.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final TransactionTemplate transactionTemplate;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.password-reset.token-expiration-minutes}")
    private int tokenExpirationMinutes;

    @Value("${app.password-reset.base-url}")
    private String resetBaseUrl;

    /**
     * Issues a reset token to the address if it matches a known user, then sends the email.
     * The token save runs in a short transaction so the DB connection isn't held during SMTP;
     * the email is sent <em>after</em> commit. Returns silently when the email is unknown to
     * avoid leaking which addresses are registered.
     */
    public void requestReset(PasswordResetRequestDto requestDto) {
        PreparedReset prepared = transactionTemplate.execute(status ->
                userRepository.findByEmailAndDeletedAtIsNull(requestDto.getEmail())
                        .map(this::issueToken)
                        .orElse(null)
        );

        if (prepared == null) {
            return;
        }

        emailService.sendPasswordResetEmail(prepared.email(), prepared.url());
    }

    /**
     * Validates the token, sets the new password, marks the token used, and revokes every
     * active refresh token for the user (so all existing sessions are ended).
     */
    @Transactional
    public void resetPassword(PasswordResetConfirmDto resetDto) {
        String tokenHash = TokenHashUtil.sha256(resetDto.getToken());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Reset token has already been used");
        }
        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }

        AppUser user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(resetDto.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenService.revokeAllForUser(user.getId());
    }

    /** Scheduled cleanup entry point. Drops expired tokens in bulk. */
    @Transactional
    public void purgeExpired() {
        passwordResetTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }

    /** Saves a fresh token row and returns the e-mail + URL the caller needs to send. */
    private PreparedReset issueToken(AppUser user) {
        String rawToken = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .tokenHash(TokenHashUtil.sha256(rawToken))
                .user(user)
                .used(false)
                .expiresAt(Instant.now().plusSeconds(tokenExpirationMinutes * 60L))
                .build();

        passwordResetTokenRepository.save(resetToken);

        return new PreparedReset(user.getEmail(), resetBaseUrl + "?token=" + rawToken);
    }

    private record PreparedReset(String email, String url) {}
}
