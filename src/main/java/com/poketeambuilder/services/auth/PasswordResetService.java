package com.poketeambuilder.services.auth;

import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.PasswordResetToken;

import com.poketeambuilder.dtos.auth.PasswordResetDto;
import com.poketeambuilder.dtos.auth.PasswordResetRequestDto;

import com.poketeambuilder.infrastructure.exceptions.InvalidTokenException;

import com.poketeambuilder.repositories.UserRepository;
import com.poketeambuilder.repositories.PasswordResetTokenRepository;

import com.poketeambuilder.utils.token.TokenHashUtil;

import java.util.UUID;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.password-reset.token-expiration-minutes}")
    private int tokenExpirationMinutes;

    @Value("${app.password-reset.base-url}")
    private String resetBaseUrl;

    @Transactional
    public void requestReset(PasswordResetRequestDto requestDto) {
        userRepository.findByEmail(requestDto.getEmail()).ifPresent(user -> {
            String rawToken = UUID.randomUUID().toString();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .tokenHash(TokenHashUtil.sha256(rawToken))
                    .user(user)
                    .used(false)
                    .expiresAt(Instant.now().plusSeconds(tokenExpirationMinutes * 60L))
                    .build();

            passwordResetTokenRepository.save(resetToken);

            String resetUrl = resetBaseUrl + "?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
        });
    }

    @Transactional
    public void resetPassword(PasswordResetDto resetDto) {
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

    @Transactional
    public void purgeExpired() {
        passwordResetTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
