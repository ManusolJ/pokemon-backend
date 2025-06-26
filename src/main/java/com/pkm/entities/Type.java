package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

@Entity
@Table(name = "types")
@Data
public class Type {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false, insertable = false)
    private Long id;

    @Column(length = 20)
    private String name;
}
