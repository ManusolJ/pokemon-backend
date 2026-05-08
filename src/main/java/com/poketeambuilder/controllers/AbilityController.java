package com.poketeambuilder.controllers;

import com.poketeambuilder.dtos.front.ability.AbilityReadDto;
import com.poketeambuilder.dtos.front.ability.AbilitySummaryDto;
import com.poketeambuilder.dtos.front.ability.AbilityFilterDto;

import com.poketeambuilder.services.query.AbilityQueryService;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("api/abilities")
public class AbilityController {
    
    private final AbilityQueryService abilityQueryService;

    @PostMapping("/filter")
    public ResponseEntity<Page<AbilityReadDto>> getAbilities(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody AbilityFilterDto filter) {
        Page<AbilityReadDto> abilities = abilityQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(abilities);
    }

    @PostMapping("summaries")
    public ResponseEntity<Page<AbilitySummaryDto>> getAbilitySummaries(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody AbilityFilterDto filter) {
        Page<AbilitySummaryDto> summaries = abilityQueryService.filterAbilitySummaries(filter, pageable);
        return ResponseEntity.ok(summaries);
    }

    @PostMapping("/id")
    public ResponseEntity<AbilityReadDto> getAbilityById(@RequestBody AbilityFilterDto filter) {
        AbilityReadDto ability = abilityQueryService.findById(filter.getId());
        return ResponseEntity.ok(ability);
    }

    @PostMapping("/count")
    public ResponseEntity<Long> getAbilityCount(@RequestBody AbilityFilterDto filter) {
        long count = abilityQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(count);
    }
}
