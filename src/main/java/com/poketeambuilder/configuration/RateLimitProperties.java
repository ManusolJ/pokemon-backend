package com.poketeambuilder.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where the auth rate limiter reads the client address from, bound from {@code app.rate-limit}.
 *
 * @param clientIpHeader header carrying the real client address, set by the edge proxy in front
 * of the app ({@code CF-Connecting-IP} behind Cloudflare).
 */
@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(String clientIpHeader) {
    
}
