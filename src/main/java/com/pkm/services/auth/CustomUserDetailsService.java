package com.pkm.services.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;

import java.util.List;

import com.pkm.configuration.security.CustomUserDetails;

import com.pkm.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NotBlank String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> {
                    String role = "ROLE_" + user.getRole().getAuthority();
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                    return new CustomUserDetails(
                            user.getUsername(),
                            user.getPassword(),
                            authorities                    );
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    
}
