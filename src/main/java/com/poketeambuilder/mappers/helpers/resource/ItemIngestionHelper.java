package com.poketeambuilder.mappers.helpers.resource;

import com.poketeambuilder.dtos.pokeapi.item.ItemSprite;
import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import com.poketeambuilder.mappers.helpers.shared.SpriteUrlRewriter;

import org.mapstruct.Named;

import org.springframework.stereotype.Component;

/** MapStruct helper bean for item ingestion. Resolves the category slug and rewrites the sprite URL. */
@Component
public class ItemIngestionHelper {

    /** Returns the category slug (e.g. {@code "held-items"}). */
    @Named("extractItemCategory")
    public String extractCategory(PokeApiResource category) {
        return category == null ? null : category.name();
    }

    /** Returns the default sprite URL with the GitHub prefix stripped. */
    @Named("extractItemSpriteUrl")
    public String extractSpriteUrl(ItemSprite sprites) {
        return sprites == null ? null : SpriteUrlRewriter.rewrite(sprites.defaultSprite());
    }
}
