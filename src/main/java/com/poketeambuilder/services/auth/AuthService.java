package com.poketeambuilder.services.auth;

import com.poketeambuilder.infrastructure.exceptions.InvalidTokenException;
import com.poketeambuilder.infrastructure.exceptions.ResourceAlreadyExistsException;

import com.poketeambuilder.dtos.auth.LoginDto;
import com.poketeambuilder.dtos.auth.RegisterDto;
import com.poketeambuilder.dtos.auth.RefreshTokenDto;
import com.poketeambuilder.dtos.auth.TokenResponseDto;

import com.poketeambuilder.entities.AppUser;

import com.poketeambuilder.repositories.AppUserRepository;

import com.poketeambuilder.mappers.implementation.UserMapper;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final AppUserRepository appUserRepository;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    private static final int ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000;

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

        return buildTokenResponse(userDetails);
    }

    public TokenResponseDto login(LoginDto loginDto) {
        String username = resolveUsername(loginDto.getIdentifier());

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, loginDto.getPassword()));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        return buildTokenResponse(userDetails);
    }

    public TokenResponseDto refresh(RefreshTokenDto refreshTokenDto) {
        String refreshToken = refreshTokenDto.refreshToken();

        String username;

        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return new TokenResponseDto(newAccessToken, refreshToken, ACCESS_TOKEN_EXPIRATION_MS);
    }

    private String resolveUsername(String identifier) {
        if (identifier.contains("@")) {
            return appUserRepository.findByEmail(identifier)
                    .map(AppUser::getUsername)
                    .orElse(identifier);
        }

        return identifier;
    }

    private TokenResponseDto buildTokenResponse(UserDetails userDetails) {
        return new TokenResponseDto(
                jwtService.generateAccessToken(userDetails),
                jwtService.generateRefreshToken(userDetails),
                ACCESS_TOKEN_EXPIRATION_MS
        );
    }
}