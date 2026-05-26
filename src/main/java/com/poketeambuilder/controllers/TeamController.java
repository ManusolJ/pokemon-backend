package com.poketeambuilder.controllers;

import com.poketeambuilder.dtos.front.team.details.TeamReadDto;
import com.poketeambuilder.dtos.front.team.details.TeamPatchDto;
import com.poketeambuilder.dtos.front.team.details.TeamCreateDto;
import com.poketeambuilder.dtos.front.team.details.TeamUpdateDto;
import com.poketeambuilder.dtos.front.team.details.TeamFilterDto;
import com.poketeambuilder.dtos.front.team.details.TeamSummaryDto;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

/**
 * Team REST surface. Routes split by visibility scope:
 * <ul>
 *   <li>{@code /public/*}: Anonymous-friendly reads of public teams. {@code SecurityConfig}
 *       permits both authenticated and anonymous access; the authenticated username (when
 *       present) lets the query layer surface per-viewer flags like "liked-by-me".</li>
 *   <li>Root + {@code /{id}*}: owner-scoped mutating operations (create / update / patch /
 *       delete / like). Require authentication.</li>
 *   <li>{@code /admin/*}:: privileged moderation, guarded by {@code @PreAuthorize} and
 *       {@code SecurityConfig}.
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamQueryService teamQueryService;
    private final TeamCommandService teamCommandService;

    /** Public catalog of teams. */
    @PostMapping("/public/filter")
    public ResponseEntity<Page<TeamSummaryDto>> getPublicTeams(@AuthenticationPrincipal UserDetails user, @PageableDefault(page = 0, size = 20, sort = "likeCount", direction = Direction.DESC) Pageable pageable, @RequestBody TeamFilterDto filter) {
        Page<TeamSummaryDto> summaries = teamQueryService.filterPublicSummaries(filter, pageable, usernameOrNull(user));
        return ResponseEntity.ok(summaries);
    }

    /** Single public team read. */
    @PostMapping("/public/id")
    public ResponseEntity<TeamReadDto> getPublicTeamById(@AuthenticationPrincipal UserDetails user, @RequestBody TeamFilterDto filter) {
        TeamReadDto team = teamQueryService.findPublicTeamById(filter.getId(), usernameOrNull(user));
        return ResponseEntity.ok(team);
    }

    /** Authenticated user's own teams (public + private). */
    @PostMapping("/me/filter")
    public ResponseEntity<Page<TeamSummaryDto>> getMyTeams(@AuthenticationPrincipal UserDetails user, @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Direction.DESC) Pageable pageable, @RequestBody TeamFilterDto filter) {
        Page<TeamSummaryDto> summaries = teamQueryService.filterOwnedSummaries(filter, pageable, user.getUsername());
        return ResponseEntity.ok(summaries);
    }

    /** Single owned-team read. 404s if the team isn't owned by the caller. */
    @PostMapping("/me/id")
    public ResponseEntity<TeamReadDto> getMyTeamById(@AuthenticationPrincipal UserDetails user, @RequestBody TeamFilterDto filter) {
        TeamReadDto team = teamQueryService.findOwnedTeamById(filter.getId(), user.getUsername());
        return ResponseEntity.ok(team);
    }

    /** Creates a team owned by the authenticated user. */
    @PostMapping
    public ResponseEntity<TeamReadDto> createTeam(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody TeamCreateDto dto) {
        Long createdTeamId = teamCommandService.createTeam(user.getUsername(), dto);
        TeamReadDto createdTeam = teamQueryService.findById(createdTeamId, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
    }

    /** Full replacement of an owned team. */
    @PutMapping("/{id}")
    public ResponseEntity<TeamReadDto> updateTeam(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @Valid @RequestBody TeamUpdateDto dto) {
        Long updatedTeamId = teamCommandService.updateTeam(user.getUsername(), id, dto);
        TeamReadDto updatedTeam = teamQueryService.findById(updatedTeamId, user.getUsername());
        return ResponseEntity.ok(updatedTeam);
    }

    /** Partial update of an owned team (metadata only). */
    @PatchMapping("/{id}")
    public ResponseEntity<TeamReadDto> patchTeam(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @Valid @RequestBody TeamPatchDto dto) {
        Long patchedTeamId = teamCommandService.patchTeam(user.getUsername(), id, dto);
        TeamReadDto patchedTeam = teamQueryService.findById(patchedTeamId, user.getUsername());
        return ResponseEntity.ok(patchedTeam);
    }

    /** Deletes an owned team. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        teamCommandService.deleteTeam(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    /** Likes a public team. */
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeTeam(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        teamCommandService.likeTeam(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    /** Removes the caller's like from a team. */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikeTeam(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        teamCommandService.unlikeTeam(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    /** Admin: delete any team. */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDeleteTeam(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        teamCommandService.adminDeleteTeam(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    private String usernameOrNull(UserDetails user) {
        return user == null ? null : user.getUsername();
    }
}
