package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pokemon")
@Data
public class Pokemon {

    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @JoinColumn(name = "type1_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Type type1;

    @JoinColumn(name = "type2_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Type type2 = null;

    @Column(name = "height")
    private BigDecimal height;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "description")
    @Lob
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

    @ManyToMany
    @JoinTable(
        name = "pokemon_abilities",
        joinColumns = @JoinColumn(name = "pokemon_id"),
        inverseJoinColumns = @JoinColumn(name = "ability_id")
    )
    private Set<Ability> abilities = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "pokemon_moves",
        joinColumns = @JoinColumn(name = "pokemon_id"),
        inverseJoinColumns = @JoinColumn(name = "move_id")
    )
    private Set<Move> moves = new HashSet<>();
}
