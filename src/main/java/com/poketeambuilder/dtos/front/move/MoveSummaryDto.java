package com.poketeambuilder.dtos.front.move;

import com.poketeambuilder.dtos.front.type.typing.TypeReadDto;

public record MoveSummaryDto(int id, String name, TypeReadDto type) {
    
}
