package com.poketeambuilder.services.auth;

import com.poketeambuilder.entities.AppUser;

import com.poketeambuilder.infrastructure.security.AuthenticatedUser;

import com.poketeambuilder.repositories.UserRepository;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Bridges {@link AppUser} into Spring Security as an {@link AuthenticatedUser}. The authority
 * list carries a single {@code ROLE_<role>} entry built from {@link AppUser#getRole()};
 * {@link AppUser#getEnabled()} is wired through to the {@code enabled} flag so disabled accounts
 * cannot authenticate.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository appUserRepository;

    @Override
    public AuthenticatedUser loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return AuthenticatedUser.of(appUser);
    }

    /** Id-based counterpart of {@link #loadUserByUsername(String)}, for callers that already hold the id. */
    public AuthenticatedUser loadUserById(Long id) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));

        return AuthenticatedUser.of(appUser);
    }
}
