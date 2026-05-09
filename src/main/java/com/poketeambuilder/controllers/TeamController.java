package com.poketeambuilder.controllers;

import com.poketeambuilder.dtos.front.team.team.TeamReadDto;
import com.poketeambuilder.dtos.front.team.team.TeamCreateDto;
import com.poketeambuilder.dtos.front.team.team.TeamFilterDto;
import com.poketeambuilder.dtos.front.team.team.TeamUpdateDto;
import com.poketeambuilder.dtos.front.team.team.TeamSummaryDto;

import com.poketeambuilder.services.query.TeamQueryService;
import com.poketeambuilder.services.command.TeamCommandService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamQueryService teamQueryService;
    private final TeamCommandService teamCommandService;

    @PostMapping("/public/filter")
    public ResponseEntity<Page<TeamSummaryDto>> getPublicTeams(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody TeamFilterDto filter) {
        Page<TeamSummaryDto> summaries = teamQueryService.filterPublicSummaries(filter, pageable);
        return ResponseEntity.ok(summaries);
    }

    @PostMapping("/public/id")
    public ResponseEntity<TeamReadDto> getPublicTeamById(@RequestBody TeamFilterDto filter) {
        TeamReadDto team = teamQueryService.findPublicTeamById(filter.getId());
        return ResponseEntity.ok(team);
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<TeamReadDto>> getTeams(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody TeamFilterDto filter) {
        Page<TeamReadDto> teams = teamQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(teams);
    }

    @PostMapping("/summaries")
    public ResponseEntity<Page<TeamSummaryDto>> getTeamSummaries(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody TeamFilterDto filter) {
        Page<TeamSummaryDto> summaries = teamQueryService.filterSummaries(filter, pageable);
        return ResponseEntity.ok(summaries);
    }

    @PostMapping("/id")
    public ResponseEntity<TeamReadDto> getTeamById(@RequestBody TeamFilterDto filter) {
        TeamReadDto team = teamQueryService.findById(filter.getId());
        return ResponseEntity.ok(team);
    }

    @PostMapping("/count")
    public ResponseEntity<Long> getTeamCount(@RequestBody TeamFilterDto filter) {
        Long teamCount = teamQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(teamCount);
    }

    @PostMapping
    public ResponseEntity<TeamReadDto> createTeam(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody TeamCreateDto dto) {
        Long createdTeamId = teamCommandService.createTeam(user.getUsername(), dto);
        TeamReadDto createdTeam = teamQueryService.findById(createdTeamId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamReadDto> updateTeam(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @Valid @RequestBody TeamUpdateDto dto) {
        Long updatedTeamId = teamCommandService.updateTeam(user.getUsername(), id, dto);
        TeamReadDto updatedTeam = teamQueryService.findById(updatedTeamId);
        return ResponseEntity.ok(updatedTeam);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        teamCommandService.deleteTeam(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeTeam(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        teamCommandService.likeTeam(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikeTeam(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        teamCommandService.unlikeTeam(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDeleteTeam(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        teamCommandService.adminDeleteTeam(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}