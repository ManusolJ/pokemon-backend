package com.pkm.services.user;

import java.time.Duration;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pkm.entities.User;
import com.pkm.utils.enums.UserRole;
import com.pkm.DTOs.user.UserCreateDTO;
import com.pkm.DTOs.user.UserDTO;
import com.pkm.DTOs.user.UserResponseDTO;
import com.pkm.repositories.UserRepository;
import com.pkm.services.auth.TokenStorageService;
import com.pkm.utils.mappers.UserMapper;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {
    private final UserRepository userRepository;

    private final UserAuthService userAuthService;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final TokenStorageService tokenStorageService;

    public UserResponseDTO createUser(@NotNull UserCreateDTO dto) {
        final String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$";
        final String username = dto.getUsername();
        final String rawPassword = dto.getPassword();

        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required.");
        }

        if (rawPassword.length() < 8 || !rawPassword.matches(passwordPattern)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters long and match the required pattern.");
        }

        final String normalizedUsername = username.toLowerCase().trim();

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists.");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(rawPassword));

        try {
            userRepository.saveAndFlush(user);

            UserResponseDTO response = userAuthService.login(normalizedUsername, rawPassword);
            tokenStorageService.storeRefreshToken(
                    response.getRefreshToken(),
                    normalizedUsername,
                    Duration.ofDays(14));

            return response;
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists.");
        }
    }

    public UserResponseDTO updateUser(@NotNull Long userId, @NotNull UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with ID '%d' not found.", userId)));

        userMapper.updateUserFromDto(userDTO, user);
        userRepository.saveAndFlush(user);

        return userMapper.toResponseDTO(user, null, null);
    }

    public boolean deleteUser(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    tokenStorageService.revokeRefreshToken(user.getUsername());
                    userRepository.delete(user);
                    return true;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with ID '%d' not found.", userId)));
    }

    public boolean resetUserPassword(Long userId, String newPassword) {
        return userRepository.findById(userId)
                .map(user -> {
                    tokenStorageService.revokeRefreshToken(user.getUsername());

                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return true;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with ID '%d' not found.", userId)));
    }

    public boolean updateUserRole(Long userId, UserRole newRole) {
        return userRepository.findById(userId)
                .map(user -> {
                    tokenStorageService.revokeRefreshToken(user.getUsername());

                    user.setRole(newRole);
                    userRepository.save(user);
                    return true;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with ID '%d' not found.", userId)));
    }

    public boolean setActiveStatus(Long userId, boolean isActive) {
        return userRepository.findById(userId)
                .map(user -> {
                    if (!isActive) {
                        tokenStorageService.revokeRefreshToken(user.getUsername());
                    }

                    user.setActive(isActive);
                    userRepository.save(user);
                    return true;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with ID '%d' not found.", userId)));
    }

    public boolean setActiveStatusForUserGroup(Long[] userIds, boolean isActive) {
        boolean allUpdated = true;
        for (Long userId : userIds) {
            try {
                setActiveStatus(userId, isActive);
            } catch (ResponseStatusException e) {
                allUpdated = false;
            }
        }
        return allUpdated;
    }
}
