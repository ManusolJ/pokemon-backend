package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.dtos.pokeapi.item.ItemApiDto;
import com.poketeambuilder.dtos.pokeapi.item.ItemSprite;

import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.LocalizedEntries;
import com.poketeambuilder.dtos.pokeapi.common.FlavorTextFallback;
import com.poketeambuilder.dtos.pokeapi.common.ItemFlavorTextEntry;

import com.poketeambuilder.mappers.helpers.shared.SpriteUrlRewriter;
import com.poketeambuilder.mappers.helpers.shared.FlavorTextSanitizer;

import org.springframework.stereotype.Component;

@Component
public class ItemIngestionHelper {

    public String extractCategory(ItemApiDto dto) {
        return dto.category() == null ? null : dto.category().name();
    }

    public String extractSpriteUrl(ItemApiDto dto) {
        ItemSprite sprite = dto.sprites();

        if (sprite == null) {
            return null;
        }

        return SpriteUrlRewriter.rewrite(sprite.defaultSprite());
    }

    public String extractDescription(ItemApiDto dto) {
        String effect = LocalizedEntries.english(dto.effectEntries())
                .map(EffectEntry::shortEffect)
                .map(FlavorTextSanitizer::clean)
                .orElse(null);

        if (effect != null && !effect.isBlank()) {
            return effect;
        }
        
        return FlavorTextFallback.pickBestForItem(dto.flavorTextEntries())
                .map(ItemFlavorTextEntry::text)
                .map(FlavorTextSanitizer::clean)
                .orElse(null);
    }
}
