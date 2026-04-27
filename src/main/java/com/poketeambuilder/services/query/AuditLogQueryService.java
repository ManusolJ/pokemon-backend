package com.poketeambuilder.services.query;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.poketeambuilder.entities.AuditLog;
import com.poketeambuilder.dtos.front.admin.audit.AuditLogFilterDto;
import com.poketeambuilder.dtos.front.admin.audit.AuditLogReadDto;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.AuditLogMapper;
import com.poketeambuilder.repositories.AuditLogRepository;
import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class AuditLogQueryService extends AbstractQueryService<AuditLog, Long, AuditLogFilterDto, AuditLogReadDto> {

    private final AuditLogMapper auditLogMapper;
    private final AuditLogRepository auditLogRepository;

    private static final String FIELD_ID = "id";
    private static final String FIELD_ACTION = "action";
    private static final String FIELD_ENTITY = "entity";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_ENTITY_ID = "entityId";
    private static final String FIELD_CREATED_AT = "createdAt";

    @Override
    protected String getEntityName() {
        return "AuditLog";
    }

    @Override
    protected ReadMapper<AuditLog, AuditLogReadDto> getMapper() {
        return auditLogMapper;
    }

    @Override
    protected BaseRepository<AuditLog, Long> getRepository() {
        return auditLogRepository;
    }

    @Override
    protected Specification<AuditLog> buildSpecification(@NotNull AuditLogFilterDto filter) {
        SpecificationBuilder<AuditLog> builder = new SpecificationBuilder<>();

        if (!filter.hasAnyCriteria()) {
            return builder.build();
        }

        if (filter.getId() != null) {
            builder.with(FIELD_ID, filter.getId(), SearchOperation.EQUAL);
        }
        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            builder.with(FIELD_USERNAME, filter.getUsername(), SearchOperation.LIKE);
        }
        if (filter.getUsernameExact() != null && !filter.getUsernameExact().isBlank()) {
            builder.with(FIELD_USERNAME, filter.getUsernameExact(), SearchOperation.EQUAL);
        }
        if (filter.getAction() != null && !filter.getAction().isBlank()) {
            builder.with(FIELD_ACTION, filter.getAction(), SearchOperation.LIKE);
        }
        if (filter.getEntity() != null && !filter.getEntity().isBlank()) {
            builder.with(FIELD_ENTITY, filter.getEntity(), SearchOperation.EQUAL);
        }
        if (filter.getEntityId() != null && !filter.getEntityId().isBlank()) {
            builder.with(FIELD_ENTITY_ID, filter.getEntityId(), SearchOperation.EQUAL);
        }

        Specification<AuditLog> spec = builder.build();

        if (filter.getDateFrom() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get(FIELD_CREATED_AT), filter.getDateFrom()));
        }
        if (filter.getDateTo() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get(FIELD_CREATED_AT), filter.getDateTo()));
        }

        return spec;
    }
}