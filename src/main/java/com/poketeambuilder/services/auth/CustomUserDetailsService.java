package com.poketeambuilder.services.auth;

import java.util.List;

import com.poketeambuilder.entities.AppUser;

import com.poketeambuilder.repositories.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Bridges {@link AppUser} into Spring Security. The authority list carries a single
 * {@code ROLE_<role>} entry built from {@link AppUser#getRole()}; {@link AppUser#getEnabled()}
 * is wired through to the {@link User}'s {@code enabled} flag so disabled accounts cannot
 * authenticate.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new User(
                appUser.getUsername(),
                appUser.getPassword(),
                appUser.getEnabled(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().getValue()))
        );
    }
}
