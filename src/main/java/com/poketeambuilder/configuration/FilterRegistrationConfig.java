package com.poketeambuilder.configuration;

import com.poketeambuilder.infrastructure.security.AuthRateLimitFilter;
import com.poketeambuilder.infrastructure.security.JwtAuthenticationFilter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.Filter;

/**
 * Keeps the security filters out of the plain servlet chain.
 *
 * <p>Boot registers every {@link Filter} bean it finds, so the two filters {@code SecurityConfig}
 * installs with {@code addFilterBefore} are also picked up as standalone servlet filters and end
 * up in the chain twice. Today that happens to be harmless — the security chain proxy runs
 * first and {@code OncePerRequestFilter} suppresses the second pass — but it leaves the order
 * dependent on a coincidence. Disabling the automatic registration makes the security chain the
 * only place either filter runs.</p>
 */
@Configuration(proxyBeanMethods = false)
public class FilterRegistrationConfig {

    @Bean
    FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(JwtAuthenticationFilter filter) {
        return disabledRegistration(filter);
    }

    @Bean
    FilterRegistrationBean<AuthRateLimitFilter> authRateLimitFilterRegistration(AuthRateLimitFilter filter) {
        return disabledRegistration(filter);
    }

    private <T extends Filter> FilterRegistrationBean<T> disabledRegistration(T filter) {
        FilterRegistrationBean<T> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
