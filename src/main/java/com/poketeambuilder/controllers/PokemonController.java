package com.poketeambuilder.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poketeambuilder.dtos.front.pokemon.common.PokemonFilterDto;
import com.poketeambuilder.dtos.front.pokemon.individual.PokemonReadDto;
import com.poketeambuilder.dtos.front.pokemon.individual.PokemonSummaryDto;
import com.poketeambuilder.services.query.PokemonQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pokemon")
public class PokemonController {
    
    private final PokemonQueryService pokemonQueryService;

    @PostMapping("/filter")
    public ResponseEntity<Page<PokemonReadDto>> getPokemon(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.ASC) Pageable pageable, @RequestBody PokemonFilterDto filter) {
        Page<PokemonReadDto> pokemon = pokemonQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(pokemon);
    }

    @PostMapping("/id")
    public ResponseEntity<PokemonReadDto> getPokemonById(@RequestBody PokemonFilterDto filter) {
        PokemonReadDto pokemon = pokemonQueryService.findById(filter.getId());
        return ResponseEntity.ok(pokemon);
    }

    @PostMapping("/summaries")
    public ResponseEntity<Page<PokemonSummaryDto>> getPokemonSummaries(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.ASC) Pageable pageable, @RequestBody PokemonFilterDto filter) {
        Page<PokemonSummaryDto> pokemonSummaries = pokemonQueryService.filterSummaries(filter, pageable);
        return ResponseEntity.ok(pokemonSummaries);
    }

    @PostMapping("/count")
    public ResponseEntity<Long> getPokemonCount(@RequestBody PokemonFilterDto filter) {
        Long pokemonCount = pokemonQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(pokemonCount);
    }

}
