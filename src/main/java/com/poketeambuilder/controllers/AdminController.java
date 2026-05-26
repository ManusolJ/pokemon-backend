package com.poketeambuilder.controllers;

import com.poketeambuilder.entities.SeedLog;

import com.poketeambuilder.dtos.front.admin.seed.SeedLogReadDto;
import com.poketeambuilder.dtos.front.admin.seed.SeedLogFilterDto;
import com.poketeambuilder.dtos.front.admin.audit.AuditLogReadDto;
import com.poketeambuilder.dtos.front.admin.audit.AuditLogFilterDto;

import com.poketeambuilder.services.query.SeedLogQueryService;
import com.poketeambuilder.services.query.AuditLogQueryService;
import com.poketeambuilder.services.command.SeedLogCommandService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Admin-only operations rooted at {@code /api/admin}.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final SeedLogCommandService seedService;
    private final SeedLogQueryService seedLogQueryService;
    private final AuditLogQueryService auditLogQueryService;

    /** Kicks off the seed pipeline synchronously and returns the persisted log row. */
    @PostMapping("/seed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeedLogReadDto> triggerSeed(@AuthenticationPrincipal UserDetails user) {
        SeedLog seedLog = seedService.executeSeed(user.getUsername());
        SeedLogReadDto seedLogReadDto = seedLogQueryService.findById(seedLog.getId());
        return ResponseEntity.ok(seedLogReadDto);
    }

    /** Paged seed-log listing for the admin dashboard. */
    @PostMapping("/seed-logs/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SeedLogReadDto>> getSeedLogs(
            @PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable,
            @RequestBody SeedLogFilterDto filter) {
        Page<SeedLogReadDto> page = seedLogQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(page);
    }

    /** Paged audit-log listing for the admin dashboard. */
    @PostMapping("/audit-logs/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogReadDto>> getAuditLogs(
            @PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable,
            @RequestBody AuditLogFilterDto filter) {
        Page<AuditLogReadDto> page = auditLogQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(page);
    }
}
