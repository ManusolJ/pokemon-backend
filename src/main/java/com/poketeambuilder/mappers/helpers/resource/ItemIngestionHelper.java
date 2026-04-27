package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.dtos.pokeapi.item.ItemSprite;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;
import com.poketeambuilder.mappers.helpers.shared.SpriteUrlRewriter;

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
}