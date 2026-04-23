package com.poketeambuilder.utils.mappers.common;

public interface ReadMapper<E, R> {
    
    R toReadDto(E entity);
}
