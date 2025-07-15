package com.pkm.configuration.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;

import lombok.Data;

import java.util.Collection;

@Data
public class CustomUserDetails implements UserDetails{
    private final String username;

    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;
}
