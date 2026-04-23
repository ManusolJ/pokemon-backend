package com.poketeambuilder.utils.mappers.common;

public interface WriteMapper<E, C> {
    
    E toEntity(C createDto);
}
