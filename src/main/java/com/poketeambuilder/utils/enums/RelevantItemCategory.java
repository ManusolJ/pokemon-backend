package com.poketeambuilder.utils.enums;

import com.poketeambuilder.services.seed.ItemSeedService;

/**
 * Subset of PokeAPI item-category slugs the seed pipeline cares about — i.e. categories that
 * matter for team-building (held items, mega stones, plates, …). Used by
 * {@link ItemSeedService} to filter the full item catalog
 * down to ones worth persisting.
 */
public enum RelevantItemCategory implements ValuedEnum {

    PLATES("plates"),
    CHOICE("choice"),
    JEWELS("jewels"),
    MEMORIES("memories"),
    IN_A_PINCH("in-a-pinch"),
    HELD_ITEMS("held-items"),
    Z_CRYSTALS("z-crystals"),
    TERA_SHARD("tera-shard"),
    MEGA_STONES("mega-stones"),
    PICKY_HEALING("picky-healing"),
    BAD_HELD_ITEMS("bad-held-items"),
    TYPE_PROTECTION("type-protection"),
    SPECIES_SPECIFIC("species-specific"),
    TYPE_ENHANCEMENT("type-enhancement");

    private final String value;

    RelevantItemCategory(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
