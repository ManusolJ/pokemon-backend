package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;

import com.poketeambuilder.entities.AuditLog;
import com.poketeambuilder.dtos.front.admin.audit.AuditLogReadDto;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface AuditLogMapper extends ReadMapper<AuditLog, AuditLogReadDto> {

    @Override
    AuditLogReadDto toReadDto(AuditLog entity);
}