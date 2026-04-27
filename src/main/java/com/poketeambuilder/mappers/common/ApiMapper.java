package com.poketeambuilder.mappers.common;

public interface ApiMapper<E, D> {
    
    E toEntity(D dto);
}
