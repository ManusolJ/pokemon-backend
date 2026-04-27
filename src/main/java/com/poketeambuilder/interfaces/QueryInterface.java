package com.poketeambuilder.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface QueryInterface<R, ID, F extends FilterDtoInterface> {

    R findById(@NotNull ID id);

    long countFilteredEntities(@Valid @NotNull F filter);

    Page<R> filterEntities(@Valid @NotNull F filter, @NotNull Pageable pageable);
}