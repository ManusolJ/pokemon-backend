package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a trainer's team of Pokémon.
 */
@Entity
@Table(name = "teams")
@Data
public class Team {

    /** Unique team identifier */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false, insertable = false)
    private Long id;

    /** Owning trainer */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User User;

    /** Team display name */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Creation timestamp */
    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    /** Update timestamp */
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    /** Pokémon members of this team */
    @OneToMany(mappedBy = "team")
    private List<TeamMember> members = new ArrayList<>();
}