package com.poketeambuilder.dtos.front.admin.seed;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import java.time.Instant;

import lombok.Getter;

@Getter
public class SeedLogFilterDto implements FilterDtoInterface {

    private Long id;

    private String status;

    private String triggeredBy;

    private String triggeredByExact;

    private Instant dateFrom;
    
    private Instant dateTo;

    @Override
    public boolean hasAnyCriteria() {
        return id != null
                || status != null
                || triggeredBy != null
                || triggeredByExact != null
                || dateFrom != null
                || dateTo != null;
    }
}