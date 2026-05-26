package com.poketeambuilder.controllers;

import com.poketeambuilder.dtos.front.type.single.TypeReadDto;
import com.poketeambuilder.dtos.front.type.single.TypeFilterDto;
import com.poketeambuilder.dtos.front.type.effectiveness.TypeEffectivenessFilterDto;
import com.poketeambuilder.dtos.front.type.effectiveness.TypeEffectivenessReadDto;

import com.poketeambuilder.services.query.TypeQueryService;
import com.poketeambuilder.services.query.TypeEffectivenessQueryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Read-only catalog endpoints for elemental types and the type-vs-type
 * effectiveness chart.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/types")
public class TypeController {

    private final TypeQueryService typeQueryService;
    private final TypeEffectivenessQueryService typeEffectivenessQueryService;

    /** Paged types matching the filter. */
    @PostMapping("/filter")
    public ResponseEntity<Page<TypeReadDto>> getTypes(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody TypeFilterDto filter) {
        Page<TypeReadDto> types = typeQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(types);
    }

    /** Single type by id (filter.id). */
    @PostMapping("/id")
    public ResponseEntity<TypeReadDto> getTypeById(@RequestBody TypeFilterDto filter) {
        TypeReadDto type = typeQueryService.findById(filter.getId());
        return ResponseEntity.ok(type);
    }

    /** Count of types matching the filter. */
    @PostMapping("/count")
    public ResponseEntity<Long> getTypeCount(@RequestBody TypeFilterDto filter) {
        long count = typeQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(count);
    }

    /** Paged type-vs-type effectiveness rows (e.g. Fire vs Grass = 2x). */
    @PostMapping("/effectiveness")
    public ResponseEntity<Page<TypeEffectivenessReadDto>> getTypeEffectiveness(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody TypeEffectivenessFilterDto filter) {
        Page<TypeEffectivenessReadDto> effectiveness = typeEffectivenessQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(effectiveness);
    }

    /** Count of effectiveness rows matching the filter. */
    @PostMapping("/effectiveness/count")
    public ResponseEntity<Long> getTypeEffectivenessCount(@RequestBody TypeEffectivenessFilterDto filter) {
        long count = typeEffectivenessQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(count);
    }
}
