package com.poketeambuilder.controllers;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;
import com.poketeambuilder.dtos.front.type.typing.TypeFilterDto;
import com.poketeambuilder.dtos.front.type.effectiveness.TypeEffectivenessFilterDto;
import com.poketeambuilder.dtos.front.type.effectiveness.TypeEffectivenessReadDto;

import com.poketeambuilder.services.query.TypeQueryService;
import com.poketeambuilder.services.query.TypeEffectivenessQueryService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/types")
@RequiredArgsConstructor
public class TypeController {
    
    private final TypeQueryService typeQueryService;
    private final TypeEffectivenessQueryService typeEffectivenessQueryService;

    @PostMapping("/filter")
    public ResponseEntity<Page<TypeReadDto>> getTypes(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody TypeFilterDto filter) {
        Page<TypeReadDto> types = typeQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(types);
    }

    @PostMapping("/id")
    public ResponseEntity<TypeReadDto> getTypeById(@RequestBody TypeFilterDto filter) {
        TypeReadDto type = typeQueryService.findById(filter.getId());
        return ResponseEntity.ok(type);
    }

    @PostMapping("/count")
    public ResponseEntity<Long> getTypeCount(@RequestBody TypeFilterDto filter) {
        long count = typeQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/effectiveness")
    public ResponseEntity<Page<TypeEffectivenessReadDto>> getTypeEffectiveness(@PageableDefault(page = 0, size = 20, direction = Direction.DESC) Pageable pageable, @RequestBody TypeEffectivenessFilterDto filter) {
        Page<TypeEffectivenessReadDto> effectiveness = typeEffectivenessQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(effectiveness);
    }

    @GetMapping("/effectiveness-matrix")
    public ResponseEntity<List<TypeEffectivenessReadDto>> getTypeEffectiveness() {
        List<TypeEffectivenessReadDto> effectiveness = typeEffectivenessQueryService.getTypeEffectivenessMatrix();
        return ResponseEntity.ok(effectiveness);
    }

    @PostMapping("/effectiveness/count")
    public ResponseEntity<Long> getTypeEffectivenessCount(@RequestBody TypeEffectivenessFilterDto filter) {
        long count = typeEffectivenessQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(count);
    }
}
