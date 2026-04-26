package com.poketeambuilder.mappers.common;

public interface SummaryMapper<E, S> {
    
    S toSummaryDto(E entity);
}
