package com.poketeambuilder.mappers.helpers.resource;

import java.util.List;

import com.poketeambuilder.utils.enums.StatName;

import com.poketeambuilder.dtos.pokeapi.pokemon.PokemonSprites;
import com.poketeambuilder.dtos.pokeapi.pokemon.PokemonStatApiDto;

import com.poketeambuilder.mappers.helpers.shared.SpriteUrlRewriter;

import org.mapstruct.Named;

import org.springframework.stereotype.Component;

/**
 * MapStruct helper bean that pulls Pokémon-form-specific fields out of the PokeAPI payload:
 * the six base stats, the four sprite / artwork URLs (default and shiny variants).
 */
@Component
public class PokemonIngestionHelper {

    @Named("extractBaseHp")
    public Integer extractBaseHp(List<PokemonStatApiDto> stats) {
        return extractStat(stats, StatName.HP);
    }

    @Named("extractBaseAtk")
    public Integer extractBaseAtk(List<PokemonStatApiDto> stats) {
        return extractStat(stats, StatName.ATTACK);
    }

    @Named("extractBaseDef")
    public Integer extractBaseDef(List<PokemonStatApiDto> stats) {
        return extractStat(stats, StatName.DEFENSE);
    }

    @Named("extractBaseSpAtk")
    public Integer extractBaseSpAtk(List<PokemonStatApiDto> stats) {
        return extractStat(stats, StatName.SPECIAL_ATTACK);
    }

    @Named("extractBaseSpDef")
    public Integer extractBaseSpDef(List<PokemonStatApiDto> stats) {
        return extractStat(stats, StatName.SPECIAL_DEFENSE);
    }

    @Named("extractBaseSpeed")
    public Integer extractBaseSpeed(List<PokemonStatApiDto> stats) {
        return extractStat(stats, StatName.SPEED);
    }

    @Named("extractSpriteDefault")
    public String extractSpriteDefault(PokemonSprites sprites) {
        return sprites == null ? null : SpriteUrlRewriter.rewrite(sprites.frontDefault());
    }

    @Named("extractSpriteShiny")
    public String extractSpriteShiny(PokemonSprites sprites) {
        return sprites == null ? null : SpriteUrlRewriter.rewrite(sprites.frontShiny());
    }

    @Named("extractArtworkUrl")
    public String extractArtworkUrl(PokemonSprites sprites) {
        if (sprites == null || sprites.other() == null || sprites.other().officialArtwork() == null) {
            return null;
        }

        return SpriteUrlRewriter.rewrite(sprites.other().officialArtwork().frontDefault());
    }

    @Named("extractArtworkShiny")
    public String extractArtworkShiny(PokemonSprites sprites) {
        if (sprites == null || sprites.other() == null || sprites.other().officialArtwork() == null) {
            return null;
        }

        return SpriteUrlRewriter.rewrite(sprites.other().officialArtwork().frontShiny());
    }

    /** Locates the requested {@link StatName} entry in the PokeAPI stat list and returns its base value. */
    private Integer extractStat(List<PokemonStatApiDto> stats, StatName stat) {
        if (stats == null) {
            return null;
        }

        return stats.stream()
                .filter(s -> s.stat() != null && stat.getValue().equals(s.stat().name()))
                .map(PokemonStatApiDto::baseStat)
                .findFirst()
                .orElse(null);
    }
}
