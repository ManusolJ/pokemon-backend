package com.poketeambuilder.controllers;

import com.poketeambuilder.dtos.front.pokemon.form.PokemonFilterDto;

import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesReadDto;
import com.poketeambuilder.dtos.front.pokemon.species.PokemonSpeciesSummaryDto;

import com.poketeambuilder.services.query.SpeciesQueryService;

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
 * Read-only catalog endpoints for canonical Pokémon species (Bulbasaur, Charmander, etc).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/species")
public class SpeciesController {

    private final SpeciesQueryService speciesQueryService;

    /** Paged species matching the filter (full read shape). */
    @PostMapping("/filter")
    public ResponseEntity<Page<PokemonSpeciesReadDto>> getSpecies(@PageableDefault(page = 0, size = 20, sort = "sortOrder", direction = Direction.ASC) Pageable pageable, @RequestBody PokemonFilterDto filter) {
        Page<PokemonSpeciesReadDto> speciesPage = speciesQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(speciesPage);
    }

    /** Single species by id (filter.id). */
    @PostMapping("/id")
    public ResponseEntity<PokemonSpeciesReadDto> getSpeciesById(@RequestBody PokemonFilterDto filter) {
        PokemonSpeciesReadDto species = speciesQueryService.findById(filter.getId());
        return ResponseEntity.ok(species);
    }

    /** Paged species as lightweight summaries. */
    @PostMapping("/summaries")
    public ResponseEntity<Page<PokemonSpeciesSummaryDto>> getSpeciesSummaries(@PageableDefault(page = 0, size = 20, sort = "sortOrder", direction = Direction.ASC) Pageable pageable, @RequestBody PokemonFilterDto filter) {
        Page<PokemonSpeciesSummaryDto> summaries = speciesQueryService.filterSummaries(filter, pageable);
        return ResponseEntity.ok(summaries);
    }

    /** Count of species matching the filter. */
    @PostMapping("/count")
    public ResponseEntity<Long> getSpeciesCount(@RequestBody PokemonFilterDto filter) {
        Long count = speciesQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(count);
    }
}
