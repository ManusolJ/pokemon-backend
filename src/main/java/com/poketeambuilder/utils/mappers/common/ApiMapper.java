package com.poketeambuilder.utils.mappers.common;

public interface ApiMapper<D, E> {
    
    E toEntity(D dto);
}
