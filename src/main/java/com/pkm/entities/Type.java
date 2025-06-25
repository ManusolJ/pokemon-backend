package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

@Entity
@Table(name = "types")
@Data
public class Type {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;
}
