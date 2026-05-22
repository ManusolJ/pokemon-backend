package com.poketeambuilder.entities;

import com.poketeambuilder.utils.enums.UserRole;

import java.time.Instant;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Application user. Authentication uses {@link #password} (bcrypt); the plain password
 * never lives on the entity. {@link #role} drives Spring Security authorization checks;
 * {@link #enabled} is the soft-disable flag used by the admin reactivate / deactivate flow.
 */
@Entity
@Getter
@Setter
@Builder
@Table(name = "app_user")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser {
    
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 30)
    @Column(name = "username", nullable = false, unique = true, length = 30)
    private String username;

    @NotBlank
    @Size(max = 255)
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    @Builder.Default
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Sets {@link #createdAt} to the current instant before the first insert. */
    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }
}
