package com.poketeambuilder.configuration;

import com.poketeambuilder.infrastructure.security.AuthEntryPoint;
import com.poketeambuilder.infrastructure.security.AuthRateLimitFilter;
import com.poketeambuilder.infrastructure.security.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

import java.util.List;

@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * Public catalogue reads, listed one by one rather than as prefix wildcards. A
     * {@code /api/pokemon/**} rule would also cover any write endpoint added under that
     * prefix later, so new routes are private until someone adds them here on purpose.
     */
    private static final String[] PUBLIC_CATALOG_READS = {
            "/api/pokemon/filter", "/api/pokemon/id", "/api/pokemon/summaries", "/api/pokemon/count",
            "/api/species/filter", "/api/species/id", "/api/species/summaries", "/api/species/count",
            "/api/moves/filter", "/api/moves/id", "/api/moves/summaries", "/api/moves/count",
            "/api/abilities/filter", "/api/abilities/id", "/api/abilities/summaries", "/api/abilities/count",
            "/api/items/filter", "/api/items/id", "/api/items/summaries", "/api/items/count",
            "/api/natures/filter", "/api/natures/id", "/api/natures/count",
            "/api/types/filter", "/api/types/id", "/api/types/count",
            "/api/types/effectiveness", "/api/types/effectiveness/count"
    };

    private static final String[] PUBLIC_TEAM_READS = {
            "/api/teams/public/filter", "/api/teams/public/id"
    };

    private final AuthEntryPoint authEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthRateLimitFilter authRateLimitFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_CATALOG_READS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_TEAM_READS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/moves/pokemon/*").permitAll()
                        .requestMatchers("/api/contact").permitAll()
                        .requestMatchers(
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(
                                "/actuator/info",
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/*/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authRateLimitFilter, JwtAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}