package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

import com.pkm.utils.enums.MoveCategory;

@Entity
@Table(name = "moves")
@Data
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, insertable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String name;

    @Column(name = "effect", nullable = false)
    @Lob
    private String effect;

    @JoinColumn(name = "type")
    @ManyToOne(fetch = FetchType.LAZY)
    private Type type;

    @Column(name = "power")
    private int power;

    @Column(name = "accuracy")
    private int accuracy;

    @Column(name = "pp")
    private int pp;

    @Column(name = "priority")
    private int priority = 0;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private MoveCategory category;
}
