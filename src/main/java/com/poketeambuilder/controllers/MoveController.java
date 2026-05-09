package com.poketeambuilder.controllers;

import com.poketeambuilder.dtos.front.move.MoveEmbedDto;
import com.poketeambuilder.dtos.front.move.MoveFilterDto;
import com.poketeambuilder.dtos.front.move.MoveReadDto;
import com.poketeambuilder.dtos.front.move.MoveSummaryDto;

import com.poketeambuilder.services.query.MoveQueryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/moves")
public class MoveController {

    private final MoveQueryService moveQueryService;

    @PostMapping("/filter")
    public ResponseEntity<Page<MoveReadDto>> getMoves(
            @PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.ASC) Pageable pageable,
            @RequestBody MoveFilterDto filter) {
        Page<MoveReadDto> moves = moveQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(moves);
    }

    @PostMapping("/summaries")
    public ResponseEntity<Page<MoveSummaryDto>> getMoveSummaries(
            @PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.ASC) Pageable pageable,
            @RequestBody MoveFilterDto filter) {
        Page<MoveSummaryDto> summaries = moveQueryService.filterSummaries(filter, pageable);
        return ResponseEntity.ok(summaries);
    }

    @PostMapping("/id")
    public ResponseEntity<MoveReadDto> getMoveById(@RequestBody MoveFilterDto filter) {
        MoveReadDto move = moveQueryService.findById(filter.getId());
        return ResponseEntity.ok(move);
    }

    @PostMapping("/count")
    public ResponseEntity<Long> getMoveCount(@RequestBody MoveFilterDto filter) {
        long count = moveQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/pokemon/{pokemonId}")
    public ResponseEntity<Page<MoveEmbedDto>> getMovesByPokemon(
            @PageableDefault(page = 0, size = 50, sort = "id.moveId", direction = Direction.ASC) Pageable pageable,
            @PathVariable Integer pokemonId) {
        Page<MoveEmbedDto> embeds = moveQueryService.filterEmbeds(pokemonId, pageable);
        return ResponseEntity.ok(embeds);
    }
}