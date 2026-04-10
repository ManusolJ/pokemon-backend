package com.poketeambuilder.entities;

import com.poketeambuilder.utils.enums.MoveCategory;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

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
@Table(name = "move")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Move {
    
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private Type type;

    @Column(name = "category", nullable = false, length = 10)
    private MoveCategory category;

    @Column(name = "pp")
    private Integer pp;

    @Column(name = "power")
    private Integer power;

    @Column(name = "accuracy")
    private Integer accuracy;

    @Column(name = "effect_description", columnDefinition = "TEXT")
    private String effectDescription;

    @NotNull
    @Builder.Default
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;
}
