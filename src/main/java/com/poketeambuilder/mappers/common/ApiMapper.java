package com.poketeambuilder.mappers.common;

public interface ApiMapper<D, E> {
    
    E toEntity(D dto);
}
