package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

import java.time.LocalDateTime;

import com.pkm.utils.enums.UserRole;

/**
 * Represents a system user/trainer.
 */
@Entity
@Table(name = "users")
@Data
public class User {

    /** Unique user identifier */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false, insertable = false)
    private Long id;

    /** Login username (unique) */
    @Column(name = "username", nullable = false, unique = true, length = 20)
    private String username;

    /** Hashed password */
    @Column(name = "password", nullable = false, length = 97)
    private String password;

    /** Profile image path */
    @Column(name = "profile_photo", nullable = false)
    private String profilePhoto = "default.png";

    /** System role (USER/MODERATOR/ADMIN) */
    @Column(name = "user_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    /** Account active status */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /** Account creation timestamp */
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    /** Last update timestamp */
    @Column(name = "updated_at", nullable = false, insertable = false)
    private LocalDateTime updatedAt;
}