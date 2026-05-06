package com.poketeambuilder.services.auth;

import com.poketeambuilder.infrastructure.exceptions.InvalidTokenException;
import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;
import com.poketeambuilder.infrastructure.exceptions.ResourceNotFoundException;
import com.poketeambuilder.dtos.auth.LoginDto;
import com.poketeambuilder.dtos.auth.RegisterDto;
import com.poketeambuilder.dtos.auth.RefreshTokenDto;
import com.poketeambuilder.dtos.auth.TokenResponseDto;

import com.poketeambuilder.entities.AppUser;
import com.poketeambuilder.entities.RefreshToken;
import com.poketeambuilder.repositories.UserRepository;
import com.poketeambuilder.utils.token.TokenHashUtil;
import com.poketeambuilder.mappers.implementation.UserMapper;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository appUserRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    private static final int ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000;
    private static final int REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000;

    @Transactional
    public TokenResponseDto register(RegisterDto registerDto) {
        if (appUserRepository.existsByUsername(registerDto.getUsername())) {
            throw new ResourceAlreadyExistsException("Username is already taken");
        }

        if (appUserRepository.existsByEmail(registerDto.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already registered");
        }

        AppUser newUser = userMapper.toEntity(registerDto);

        newUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        appUserRepository.save(newUser);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(newUser.getUsername());

        return buildTokenResponse(userDetails, newUser);
    }

    public TokenResponseDto login(LoginDto loginDto) {
        AppUser user = resolveUsername(loginDto.getIdentifier());

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), loginDto.getPassword()));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());

        return buildTokenResponse(userDetails, user);
    }

    @Transactional
    public void logout(RefreshTokenDto refreshTokenDto) {
        RefreshToken storedToken = refreshTokenService.findByRawToken(refreshTokenDto.refreshToken());

        refreshTokenService.revokeFamily(storedToken.getFamilyId());
    }

    @Transactional
    public TokenResponseDto refresh(RefreshTokenDto refreshTokenDto) {
        String rawToken = refreshTokenDto.refreshToken();

        RefreshToken storedToken = refreshTokenService.findByRawToken(rawToken);

        if (storedToken.isRevoked()) {
            refreshTokenService.revokeFamily(storedToken.getFamilyId());
            throw new InvalidTokenException("Refresh token reuse detected — all sessions revoked");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token expired");
        }

        refreshTokenService.revoke(storedToken);

        UserDetails userDetails = customUserDetailsService
                .loadUserByUsername(jwtService.extractUsername(rawToken));

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        RefreshToken newEntity = RefreshToken.builder()
                .tokenHash(TokenHashUtil.sha256(newRefreshToken))
                .user(storedToken.getUser())
                .familyId(storedToken.getFamilyId())
                .revoked(false)
                .expiresAt(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION_MS))
                .build();

        refreshTokenService.create(storedToken.getUser(), newRefreshToken, newEntity.getFamilyId(), newEntity.getExpiresAt());

        return new TokenResponseDto(newAccessToken, newRefreshToken, ACCESS_TOKEN_EXPIRATION_MS);
    }

    private AppUser resolveUsername(String identifier) {
        if (identifier.contains("@")) {
            return appUserRepository.findByEmail(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + identifier));
        }

        return appUserRepository.findByUsername(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + identifier));
    }

    private TokenResponseDto buildTokenResponse(UserDetails userDetails, AppUser user) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        RefreshToken entity = RefreshToken.builder()
                .tokenHash(TokenHashUtil.sha256(refreshToken))
                .user(user)
                .familyId(UUID.randomUUID()) 
                .revoked(false)
                .expiresAt(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION_MS))
                .build();

        refreshTokenService.create(user, refreshToken, entity.getFamilyId(), entity.getExpiresAt());

        return new TokenResponseDto(accessToken, refreshToken, ACCESS_TOKEN_EXPIRATION_MS);
    }
}