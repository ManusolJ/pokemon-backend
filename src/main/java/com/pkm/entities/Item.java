package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

@Entity
@Table(name = "items")
@Data
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false, insertable = false)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "effect", nullable = false)
    @Lob
    private String effect;
}
