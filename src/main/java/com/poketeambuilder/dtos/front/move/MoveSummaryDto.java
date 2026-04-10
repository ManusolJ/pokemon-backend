package com.poketeambuilder.dtos.front.move;

import com.poketeambuilder.dtos.front.type.type.TypeReadDto;

public record MoveSummaryDto(long id, String name, TypeReadDto type) {
    
}
