package com.poketeambuilder.utils.pokeapi;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.poketeambuilder.dtos.pokeapi.common.FlavorTextEntry;
import com.poketeambuilder.dtos.pokeapi.common.MoveFlavorTextEntry;
import com.poketeambuilder.dtos.pokeapi.common.ItemFlavorTextEntry;
import com.poketeambuilder.dtos.pokeapi.common.AbilityFlavorTextEntry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Picks the "best" English flavor-text entry from a list. Pokémon resources expose flavor
 * text per game version; we prefer the most recent generation that has an entry, falling
 * back to older releases when newer ones are missing. The priority lists below order the
 * games newest-to-oldest.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlavorTextFallback {

    private static final List<String> VERSION_PRIORITY = List.of(
            "scarlet", "violet",
            "sword", "shield",
            "legends-arceus",
            "brilliant-diamond", "shining-pearl",
            "lets-go-pikachu", "lets-go-eevee",
            "ultra-sun", "ultra-moon",
            "sun", "moon",
            "omega-ruby", "alpha-sapphire",
            "x", "y",
            "black-2", "white-2",
            "black", "white",
            "heartgold", "soulsilver",
            "platinum",
            "diamond", "pearl",
            "emerald",
            "ruby", "sapphire",
            "crystal",
            "gold", "silver",
            "yellow",
            "red", "blue"
    );

    private static final List<String> VERSION_GROUP_PRIORITY = List.of(
            "scarlet-violet",
            "sword-shield",
            "brilliant-diamond-shining-pearl",
            "sun-moon",
            "ultra-sun-ultra-moon",
            "omega-ruby-alpha-sapphire",
            "x-y",
            "black-2-white-2",
            "black-white",
            "heartgold-soulsilver",
            "platinum",
            "diamond-pearl",
            "emerald",
            "ruby-sapphire",
            "crystal",
            "gold-silver",
            "yellow",
            "red-blue"
    );

    /** Picks the species flavor text from the most recent game release available. */
    public static Optional<FlavorTextEntry> pickBestForSpecies(List<FlavorTextEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }

        Map<String, FlavorTextEntry> byVersion = entries.stream()
                .filter(e -> e.language() != null && "en".equals(e.language().name()))
                .filter(e -> e.version() != null && e.version().name() != null)
                .collect(Collectors.toMap(
                        e -> e.version().name(),
                        Function.identity(),
                        (a, b) -> a
                ));

        for (String version : VERSION_PRIORITY) {
            FlavorTextEntry entry = byVersion.get(version);
            if (entry != null) {
                return Optional.of(entry);
            }
        }

        return byVersion.values().stream().findFirst();
    }

    /** Picks the ability flavor text from the most recent version group available. */
    public static Optional<AbilityFlavorTextEntry> pickBestForAbility(List<AbilityFlavorTextEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }

        Map<String, AbilityFlavorTextEntry> byVersion = entries.stream()
                .filter(e -> e.language() != null && "en".equals(e.language().name()))
                .filter(e -> e.versionGroup() != null && e.versionGroup().name() != null)
                .collect(Collectors.toMap(
                        e -> e.versionGroup().name(),
                        Function.identity(),
                        (a, b) -> a
                ));

        for (String version : VERSION_GROUP_PRIORITY) {
            AbilityFlavorTextEntry entry = byVersion.get(version);
            if (entry != null) {
                return Optional.of(entry);
            }
        }

        return byVersion.values().stream().findFirst();
    }

    /** Picks the item flavor text from the most recent version group available. */
    public static Optional<ItemFlavorTextEntry> pickBestForItem(List<ItemFlavorTextEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }

        Map<String, ItemFlavorTextEntry> byVersion = entries.stream()
                .filter(e -> e.language() != null && "en".equals(e.language().name()))
                .filter(e -> e.versionGroup() != null && e.versionGroup().name() != null)
                .collect(Collectors.toMap(
                        e -> e.versionGroup().name(),
                        Function.identity(),
                        (a, b) -> a
                ));

        for (String version : VERSION_GROUP_PRIORITY) {
            ItemFlavorTextEntry entry = byVersion.get(version);
            if (entry != null) {
                return Optional.of(entry);
            }
        }

        return byVersion.values().stream().findFirst();
    }

    /** Picks the move flavor text from the most recent version group available. */
    public static Optional<MoveFlavorTextEntry> pickBestForMove(List<MoveFlavorTextEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }

        Map<String, MoveFlavorTextEntry> byVersion = entries.stream()
                .filter(e -> e.language() != null && "en".equals(e.language().name()))
                .filter(e -> e.versionGroup() != null && e.versionGroup().name() != null)
                .collect(Collectors.toMap(
                        e -> e.versionGroup().name(),
                        Function.identity(),
                        (a, b) -> a
                ));

        for (String version : VERSION_GROUP_PRIORITY) {
            MoveFlavorTextEntry entry = byVersion.get(version);
            if (entry != null) {
                return Optional.of(entry);
            }
        }

        return byVersion.values().stream().findFirst();
    }
}
