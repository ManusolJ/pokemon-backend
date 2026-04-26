package com.poketeambuilder.mappers.helpers.resource;


import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.poketeambuilder.dtos.pokeapi.pokemon.PokemonApiDto;
import com.poketeambuilder.dtos.pokeapi.pokemon.PokemonSprites;
import com.poketeambuilder.dtos.pokeapi.type.PokemonTypeApiDto;
import com.poketeambuilder.dtos.pokeapi.pokemon.PokemonStatApiDto;

import com.poketeambuilder.utils.enums.StatName;

import com.poketeambuilder.mappers.helpers.shared.SpriteUrlRewriter;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PokemonIngestionHelper {

    public PokeApiResource extractTypes(List<PokemonTypeApiDto> types, int slot) {
        if (types == null){
            return null;
        }

        return types.stream()
                .filter(t -> t.slot() != null && t.slot() == slot)
                .map(PokemonTypeApiDto::type)
                .findFirst()
                .orElse(null);
    }

    public Integer extractStat(PokemonApiDto dto, StatName stat) {
        if (dto.stats() == null || stat == null) {
            return null;
        }

        return dto.stats().stream()
                .filter(s -> s.stat() != null && stat.getValue().equals(s.stat().name()))
                .map(PokemonStatApiDto::baseStat)
                .findFirst()
                .orElse(null);
    }

    public String extractSpriteDefault(PokemonApiDto dto) {
        if (dto.sprites() == null){
            return null;
        }

        return SpriteUrlRewriter.rewrite(dto.sprites().frontDefault());
    }

    public String extractSpriteShiny(PokemonApiDto dto) {
        if (dto.sprites() == null){
            return null;
        }

        return SpriteUrlRewriter.rewrite(dto.sprites().frontShiny());
    }

    public String extractArtworkUrl(PokemonApiDto dto) {
        PokemonSprites sprites = dto.sprites();
        
        if (sprites == null || sprites.other() == null || sprites.other().officialArtwork() == null) {
            return null;
        }

        return SpriteUrlRewriter.rewrite(sprites.other().officialArtwork().frontDefault());
    }
}
