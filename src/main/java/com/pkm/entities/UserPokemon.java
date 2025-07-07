package com.pkm.entities;

import jakarta.persistence.*;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_pokemon")
@Data
public class UserPokemon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false, insertable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "pokemon_id", nullable = false)
    private Pokemon pokemon;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "ability_id")
    private Ability ability;

    @ManyToOne
    @JoinColumn(name = "nature_id")
    private Nature nature;

    @ManyToOne
    @JoinColumn(name = "teratype_id")
    private Type teratype;

    @ManyToMany
    @JoinTable(
        name = "pokemon_movesets",
        joinColumns        = @JoinColumn(name = "pokemon_id"),
        inverseJoinColumns = @JoinColumn(name = "move_id")
    )
    private Set<Move> moves = new HashSet<>();

    @Column(name = "isShiny", nullable = false)
    private boolean shiny = false;

    @Column(name = "level", nullable = false)
    private int level = 50;

    @Column(name = "hp_ev")
    private int hpEV;

    @Column(name = "attack_ev")
    private int attackEV;

    @Column(name = "defense_ev")
    private int defenseEV;

    @Column(name = "sp_attack_ev")
    private int spAttackEV;

    @Column(name = "sp_defense_ev")
    private int spDefenseEV;

    @Column(name = "speed_ev")
    private int speedEV;

    @Column(name = "hp_iv")
    private int hpIV;

    @Column(name = "attack_iv")
    private int attackIV;

    @Column(name = "defense_iv")
    private int defenseIV;

    @Column(name = "sp_attack_iv")
    private int spAttackIV;

    @Column(name = "sp_defense_iv")
    private int spDefenseIV;

    @Column(name = "speed_iv")
    private int speedIV;
}
