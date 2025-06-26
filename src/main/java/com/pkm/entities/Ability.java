package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

@Entity
@Table(name = "abilities")
@Data
public class Ability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false, insertable = false)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "effect", nullable = false)
    @Lob
    private String effect;
}
