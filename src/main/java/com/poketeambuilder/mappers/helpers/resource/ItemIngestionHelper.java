package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.dtos.pokeapi.item.ItemSprite;

import com.poketeambuilder.dtos.pokeapi.common.EffectEntry;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.dtos.pokeapi.common.LocalizedEntries;
import com.poketeambuilder.dtos.pokeapi.common.FlavorTextFallback;
import com.poketeambuilder.dtos.pokeapi.common.ItemFlavorTextEntry;

import com.poketeambuilder.mappers.helpers.shared.SpriteUrlRewriter;
import com.poketeambuilder.mappers.helpers.shared.FlavorTextSanitizer;

import java.util.List;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class ItemIngestionHelper {

    @Named("extractItemCategory")
    public String extractCategory(PokeApiResource category) {
        return category == null ? null : category.name();
    }

    @Named("extractItemSpriteUrl")
    public String extractSpriteUrl(ItemSprite sprites) {
        return sprites == null ? null : SpriteUrlRewriter.rewrite(sprites.defaultSprite());
    }

    @Named("extractItemEffect")
    public String extractEffect(List<EffectEntry> effectEntries) {
        return LocalizedEntries.english(effectEntries)
                .map(EffectEntry::shortEffect)
                .map(FlavorTextSanitizer::clean)
                .orElse(null);
    }

    @Named("extractItemDescription")
    public String extractDescription(List<ItemFlavorTextEntry> flavorTextEntries) {
        return FlavorTextFallback.pickBestForItem(flavorTextEntries)
                .map(ItemFlavorTextEntry::text)
                .map(FlavorTextSanitizer::clean)
                .orElse(null);
    }
}