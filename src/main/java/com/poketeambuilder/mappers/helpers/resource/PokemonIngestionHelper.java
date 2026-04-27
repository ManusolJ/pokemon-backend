package com.poketeambuilder.mappers.helpers.resource;


import com.poketeambuilder.dtos.pokeapi.pokemon.PokemonSprites;
import com.poketeambuilder.dtos.pokeapi.pokemon.PokemonStatApiDto;

import com.poketeambuilder.utils.enums.StatName;

import com.poketeambuilder.mappers.helpers.shared.SpriteUrlRewriter;

import java.util.List;

import org.mapstruct.Named;

import org.springframework.stereotype.Component;

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

    @Named("extractSpriteDefault")
    public String extractSpriteDefault(PokemonSprites sprites) {
        if (sprites == null) {
            return null;
        }

        return SpriteUrlRewriter.rewrite(sprites.frontDefault());
    }

    @Named("extractSpriteShiny")
    public String extractSpriteShiny(PokemonSprites sprites) {
        if (sprites == null) {
            return null;
        }

        return SpriteUrlRewriter.rewrite(sprites.frontShiny());
    }

    @Named("extractArtworkUrl")
    public String extractArtworkUrl(PokemonSprites sprites) {
        if (sprites == null || sprites.other() == null || sprites.other().officialArtwork() == null) {
            return null;
        }

        return SpriteUrlRewriter.rewrite(sprites.other().officialArtwork().frontDefault());
    }
}