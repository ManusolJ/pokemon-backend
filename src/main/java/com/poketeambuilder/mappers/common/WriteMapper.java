package com.poketeambuilder.mappers.common;

public interface WriteMapper<E, C> {
    
    E toEntity(C createDto);
}
