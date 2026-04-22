package com.poketeambuilder.dtos.REST.common;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private FlavorTextFallback() {}

    public static Optional<FlavorTextEntry> pickBest(List<FlavorTextEntry> entries) {
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
}
