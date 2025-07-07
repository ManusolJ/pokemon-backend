package com.pkm.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.Data;

import com.pkm.utils.enums.UserRole;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 20)
    private String username;

    @Column(name = "password", nullable = false, length = 97)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "profile_photo", nullable = false)
    private String profilePhoto;

    @Column(name = "user_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime updatedAt;
}
