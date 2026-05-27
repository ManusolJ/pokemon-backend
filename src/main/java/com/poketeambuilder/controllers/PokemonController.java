package com.poketeambuilder.controllers;

import com.poketeambuilder.dtos.front.pokemon.form.PokemonReadDto;
import com.poketeambuilder.dtos.front.pokemon.form.PokemonFilterDto;
import com.poketeambuilder.dtos.front.pokemon.form.PokemonSummaryDto;

import com.poketeambuilder.services.query.PokemonQueryService;

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
 * Read-only catalog endpoints for Pokémon forms.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonQueryService pokemonQueryService;

    /** Paged Pokémon forms matching the filter. */
    @PostMapping("/filter")
    public ResponseEntity<Page<PokemonReadDto>> getPokemon(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.ASC) Pageable pageable, @RequestBody PokemonFilterDto filter) {
        Page<PokemonReadDto> pokemon = pokemonQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(pokemon);
    }

    /** Single Pokémon form by id (filter.id). */
    @PostMapping("/id")
    public ResponseEntity<PokemonReadDto> getPokemonById(@RequestBody PokemonFilterDto filter) {
        PokemonReadDto pokemon = pokemonQueryService.findById(filter.getId());
        return ResponseEntity.ok(pokemon);
    }

    /** Paged Pokémon forms as lightweight summaries. */
    @PostMapping("/summaries")
    public ResponseEntity<Page<PokemonSummaryDto>> getPokemonSummaries(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.ASC) Pageable pageable, @RequestBody PokemonFilterDto filter) {
        Page<PokemonSummaryDto> pokemonSummaries = pokemonQueryService.filterSummaries(filter, pageable);
        return ResponseEntity.ok(pokemonSummaries);
    }

    /** Count of Pokémon forms matching the filter. */
    @PostMapping("/count")
    public ResponseEntity<Long> getPokemonCount(@RequestBody PokemonFilterDto filter) {
        Long pokemonCount = pokemonQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(pokemonCount);
    }
}
