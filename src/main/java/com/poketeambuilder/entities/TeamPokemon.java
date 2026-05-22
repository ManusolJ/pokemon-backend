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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * A configured Pokémon occupying one of the six slots of a {@link Team}. Holds the build:
 * level, nature, ability, held item, tera type, EV/IV spreads, and shininess.
 *
 * <p>Per-field ranges (slot 1–6, level 1–100, EV 0–252, IV 0–31) are enforced by Bean
 * Validation through {@link Min} / {@link Max}; the cross-field EV-total ≤ 510 rule is
 * enforced by {@link #validateEvTotal()}. All these constraints are mirrored at the database
 * layer (CHECK constraints).</p>
 */
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
    @Min(1)
    @Max(6)
    @Column(name = "slot_position", nullable = false)
    private Integer slot;

    @Size(max = 12)
    @Column(name = "nickname", length = 12)
    private String nickname;

    @NotNull
    @Min(1)
    @Max(100)
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
    @JoinColumn(name = "tera_type_id")
    private Type teraType;

    @NotNull
    @Min(0)
    @Max(252)
    @Builder.Default
    @Column(name = "ev_hp", nullable = false)
    private Integer evHp = 0;

    @NotNull
    @Min(0)
    @Max(252)
    @Builder.Default
    @Column(name = "ev_atk", nullable = false)
    private Integer evAtk = 0;

    @NotNull
    @Min(0)
    @Max(252)
    @Builder.Default
    @Column(name = "ev_def", nullable = false)
    private Integer evDef = 0;

    @NotNull
    @Min(0)
    @Max(252)
    @Builder.Default
    @Column(name = "ev_sp_atk", nullable = false)
    private Integer evSpAtk = 0;

    @NotNull
    @Min(0)
    @Max(252)
    @Builder.Default
    @Column(name = "ev_sp_def", nullable = false)
    private Integer evSpDef = 0;

    @NotNull
    @Min(0)
    @Max(252)
    @Builder.Default
    @Column(name = "ev_speed", nullable = false)
    private Integer evSpeed = 0;

    @NotNull
    @Min(0)
    @Max(31)
    @Builder.Default
    @Column(name = "iv_hp", nullable = false)
    private Integer ivHp = 31;

    @NotNull
    @Min(0)
    @Max(31)
    @Builder.Default
    @Column(name = "iv_atk", nullable = false)
    private Integer ivAtk = 31;

    @NotNull
    @Min(0)
    @Max(31)
    @Builder.Default
    @Column(name = "iv_def", nullable = false)
    private Integer ivDef = 31;

    @NotNull
    @Min(0)
    @Max(31)
    @Builder.Default
    @Column(name = "iv_sp_atk", nullable = false)
    private Integer ivSpAtk = 31;

    @NotNull
    @Min(0)
    @Max(31)
    @Builder.Default
    @Column(name = "iv_sp_def", nullable = false)
    private Integer ivSpDef = 31;

    @NotNull
    @Min(0)
    @Max(31)
    @Builder.Default
    @Column(name = "iv_speed", nullable = false)
    private Integer ivSpeed = 31;

    /**
     * Enforces the cross-field rule that the six EV stats sum to at most 510. Per-field ranges are handled
     * by {@link Min} / {@link Max} above. The DB has the same rule as {@code chk_ev_total}.
     */
    @PrePersist
    @PreUpdate
    private void validateEvTotal() {
        if (evHp + evAtk + evDef + evSpAtk + evSpDef + evSpeed > 510) {
            throw new IllegalArgumentException("Total EVs cannot exceed 510");
        }
    }
}
