package com.poketeambuilder.dtos.front.move;

import com.poketeambuilder.dtos.front.type.type.TypeReadDto;

public record MoveReadDto(long id, String name, TypeReadDto type, String category, Integer pp, Integer power, Integer accuracy, Integer priority, String effectDescription) {
    
}
