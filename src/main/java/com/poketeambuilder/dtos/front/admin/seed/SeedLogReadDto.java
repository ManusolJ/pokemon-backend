package com.poketeambuilder.dtos.front.admin.seed;

import java.time.Instant;

public record SeedLogReadDto(
    long id,
    Instant startedAt,
    Instant completedAt,
    Integer entriesAdded,
    Integer errors,
    String status,
    String triggeredBy
) {}