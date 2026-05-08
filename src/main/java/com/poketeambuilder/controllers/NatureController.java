package com.poketeambuilder.controllers;

import com.poketeambuilder.dtos.front.nature.NatureReadDto;
import com.poketeambuilder.dtos.front.nature.NatureFilterDto;

import com.poketeambuilder.services.query.NatureQueryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import org.springframework.http.ResponseEntity;

import org.springframework.data.web.PageableDefault;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/natures")
public class NatureController {

    private final NatureQueryService natureQueryService;

    @PostMapping("/filter")
    public ResponseEntity<Page<NatureReadDto>> getNatures(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody NatureFilterDto filter) {
        Page<NatureReadDto> natures = natureQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(natures);
    }

    @PostMapping("/id")
    public ResponseEntity<NatureReadDto> getNatureById(@RequestBody NatureFilterDto filter) {
        NatureReadDto nature = natureQueryService.findById(filter.getId());
        return ResponseEntity.ok(nature);
    }

    @PostMapping("/count")
    public ResponseEntity<Long> getNatureCount(@RequestBody NatureFilterDto filter) {
        long count = natureQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(count);
    }
}
