package com.poketeambuilder.mappers.common;

public interface ReadMapper<E, R> {
    
    R toReadDto(E entity);
}
