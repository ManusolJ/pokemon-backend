package com.poketeambuilder.entities;

import com.poketeambuilder.utils.enums.PokemonGender;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@Builder
@Table(name = "team_pokemon")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamPokemon {
    
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokemon_id", nullable = false)
    private Pokemon pokemon;

    @NotNull
    @Column(name = "slot_position", nullable = false)
    private Integer slot;

    @Size(max = 12)
    @Column(name = "nickname", length = 12)
    private String nickname;

    @NotNull
    @Builder.Default
    @Column(name = "level", nullable = false)
    private Integer level = 100;

    @Column(name = "gender")
    private PokemonGender gender;

    @NotNull
    @Builder.Default
    @Column(name = "is_shiny", nullable = false)
    private Boolean shiny = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nature_id") 
    private Nature nature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ability_id", nullable = false)
    private Ability ability;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item heldItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tera_type")
    private Type teratype;

    @NotNull
    @Builder.Default
    @Column(name = "ev_hp", nullable = false)
    private Integer evHp = 0;

    @NotNull
    @Builder.Default
    @Column(name = "ev_atk", nullable = false)
    private Integer evAtk = 0;

    @NotNull
    @Builder.Default
    @Column(name = "ev_def", nullable = false)
    private Integer evDef = 0;

    @NotNull
    @Builder.Default
    @Column(name = "ev_sp_atk", nullable = false)
    private Integer evSpAtk = 0;

    @NotNull
    @Builder.Default
    @Column(name = "ev_sp_def", nullable = false)
    private Integer evSpDef = 0;

    @NotNull
    @Builder.Default
    @Column(name = "ev_speed", nullable = false)
    private Integer evSpeed = 0;

    @NotNull
    @Builder.Default
    @Column(name = "iv_hp", nullable = false)
    private Integer ivHp = 31;

    @NotNull
    @Builder.Default
    @Column(name = "iv_atk", nullable = false)
    private Integer ivAtk = 31;

    @NotNull
    @Builder.Default
    @Column(name = "iv_def", nullable = false)
    private Integer ivDef = 31;

    @NotNull
    @Builder.Default
    @Column(name = "iv_sp_atk", nullable = false)
    private Integer ivSpAtk = 31;

    @NotNull
    @Builder.Default
    @Column(name = "iv_sp_def", nullable = false)
    private Integer ivSpDef = 31;

    @NotNull
    @Builder.Default
    @Column(name = "iv_speed", nullable = false)
    private Integer ivSpeed = 31;

    @PrePersist
    @PreUpdate
    private void validate() {
        validateIv();
        validateEvs();
        validateSlot();
        validateLevel();
    }

    private void validateSlot() {
        if (slot < 1 || slot > 6) {
            throw new IllegalArgumentException("Slot position must be between 1 and 6");
        }
    }

    private void validateLevel() {
        if (level < 1 || level > 100) {
            throw new IllegalArgumentException("Level must be between 1 and 100");
        }
    }

    private void validateEvs() {
        if (evHp < 0 || evHp > 252 ||
            evAtk < 0 || evAtk > 252 ||
            evDef < 0 || evDef > 252 ||
            evSpAtk < 0 || evSpAtk > 252 ||
            evSpDef < 0 || evSpDef > 252 ||
            evSpeed < 0 || evSpeed > 252) {
            throw new IllegalArgumentException("EVs must be between 0 and 252");
        }
        if (evHp + evAtk + evDef + evSpAtk + evSpDef + evSpeed > 510) {
            throw new IllegalArgumentException("Total EVs cannot exceed 510");
        }
    }

    private void validateIv() {
        if (ivHp < 0 || ivHp > 31 ||
            ivAtk < 0 || ivAtk > 31 ||
            ivDef < 0 || ivDef > 31 ||
            ivSpAtk < 0 || ivSpAtk > 31 ||
            ivSpDef < 0 || ivSpDef > 31 ||
            ivSpeed < 0 || ivSpeed > 31) {
            throw new IllegalArgumentException("IVs must be between 0 and 31");
        }
    }
}