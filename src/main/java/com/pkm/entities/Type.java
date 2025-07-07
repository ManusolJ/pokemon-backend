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
    private int id;

    @Column(length = 20, nullable = false)
    private String name;
}
