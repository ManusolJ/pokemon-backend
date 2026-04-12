package com.poketeambuilder.dtos.front.admin.audit;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import java.time.Instant;

import lombok.Getter;

@Getter
public class AuditLogFilterDto implements FilterDtoInterface {

    private Long id;

    private String username;

    private String usernameExact;

    private String action;

    private String entity;

    private String entityId;

    private Instant dateFrom;
    
    private Instant dateTo;

    @Override
    public boolean hasAnyCriteria() {
        return id != null
                || username != null
                || usernameExact != null
                || action != null
                || entity != null
                || entityId != null
                || dateFrom != null
                || dateTo != null;
    }
}