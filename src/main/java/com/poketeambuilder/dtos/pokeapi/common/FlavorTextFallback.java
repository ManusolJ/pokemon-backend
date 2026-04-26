package com.poketeambuilder.dtos.pokeapi.common;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
}
