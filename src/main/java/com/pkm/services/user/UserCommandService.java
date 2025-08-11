package com.pkm.services.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pkm.entities.User;
import com.pkm.utils.enums.UserRole;
import com.pkm.DTOs.user.UserCreateDTO;
import com.pkm.utils.mappers.UserMapper;
import com.pkm.DTOs.user.UserResponseDTO;
import com.pkm.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {
    private final UserRepository userRepository;

    private final UserAuthService userAuthService;

    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO createUser(UserCreateDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists.");
        }

        User user = new User();

        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        userRepository.save(user);

        return userAuthService.login(user.getUsername(), userDTO.getPassword());
    }

    public UserResponseDTO updateUser(Long userId, UserResponseDTO userResponseDTO) {
        // Validate and update user details
        return null; // Placeholder for actual implementation
    }

    public boolean deleteUser(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    userRepository.delete(user);
                    return true;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with ID '%d' not found.", userId)));
    }

    public boolean resetUserPassword(Long userId, String newPassword) {
        return userRepository.findById(userId)
                .map(user -> {
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
