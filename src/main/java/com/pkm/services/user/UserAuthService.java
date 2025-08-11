package com.pkm.services.user;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.pkm.DTOs.user.UserResponseDTO;
import com.pkm.entities.User;
import com.pkm.repositories.UserRepository;
import com.pkm.services.auth.JWTService;
import com.pkm.utils.mappers.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticate with username/password and return user payload + tokens.
     */
    public UserResponseDTO login(String username, String rawPassword) throws AuthenticationException {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, rawPassword));

        if (!auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }

        // If authentication passes, load the domain user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Map<String, Object> claims = Map.of("role", user.getRole().getAuthority());

        String accessToken = jwtService.generateAccessToken(user.getUsername(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername(), claims);

        return userMapper.toResponseDTO(user, accessToken, refreshToken);
    }

    /**
     * Refresh tokens using a valid refresh token.
     */
    public UserResponseDTO refresh(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), java.util.Collections.emptyList()))) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Map<String, Object> claims = Map.of("role", user.getRole().getAuthority());
        String newAccess = jwtService.generateAccessToken(user.getUsername(), claims);
        String newRefresh = jwtService.generateRefreshToken(user.getUsername(), claims);
        return userMapper.toResponseDTO(user, newAccess, newRefresh);
    }
}
