package com.pkm.services.auth;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenStorageService {
    private final RedisTemplate<String, String> redisTemplate;

    public void storeRefreshToken(String token, String username, Duration ttl) {
        redisTemplate.opsForValue().set(
                "refresh:" + username,
                token,
                ttl);
    }

    public boolean isValidRefreshToken(String token, String username) {
        String storedToken = redisTemplate.opsForValue().get("refresh:" + username);
        return token.equals(storedToken);
    }

    public void revokeRefreshToken(String username) {
        redisTemplate.delete("refresh:" + username);
    }

}