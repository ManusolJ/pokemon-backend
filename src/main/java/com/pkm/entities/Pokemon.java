package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "pokemon")
@Data
public class Pokemon {

    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "type1", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Type type1;

    @Column(name = "type2")
    @ManyToOne(fetch = FetchType.LAZY)
    private Type type2 = null;

    @Column(name = "height")
    private BigDecimal height;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "description")
    private String description;

    @Column(name = "hp")
    private int hp;

    @Column(name = "attack")
    private int attack;

    @Column(name = "special_attack")
    private int specialAttack;

    @Column(name = "special_defense")
    private int specialDefense;

    @Column(name = "speed")
    private int speed;
}
