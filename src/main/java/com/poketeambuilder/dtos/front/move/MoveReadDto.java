package com.poketeambuilder.dtos.front.move;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;

public record MoveReadDto(
    int id,
    String name,
    String category,
    TypeReadDto type,
    Integer pp,
    Integer power,
    Integer accuracy,
    Integer priority,
    String effectDescription
) {
    
}
