package com.poketeambuilder.utils.mappers.common;

public interface SummaryMapper<E, S> {
    
    S toSummaryDto(E entity);
}
