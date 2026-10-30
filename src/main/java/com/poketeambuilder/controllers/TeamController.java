package com.poketeambuilder.controllers;

import java.util.Set;

import com.poketeambuilder.dtos.front.team.details.TeamReadDto;
import com.poketeambuilder.dtos.front.team.details.TeamPatchDto;
import com.poketeambuilder.dtos.front.team.details.TeamCreateDto;
import com.poketeambuilder.dtos.front.team.details.TeamUpdateDto;
import com.poketeambuilder.dtos.front.team.details.TeamFilterDto;
import com.poketeambuilder.dtos.front.team.details.TeamSummaryDto;

import com.poketeambuilder.infrastructure.security.AuthenticatedUser;

import com.poketeambuilder.services.query.TeamQueryService;
import com.poketeambuilder.services.command.TeamCommandService;

import com.poketeambuilder.utils.pagination.SortWhitelist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.GetMapping;
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
 *   <li>{@code /shared/{slug}}: anonymous-friendly read-only view of any team, private ones
 *       included, through its share link.</li>
 *   <li>{@code /public/*}: Anonymous-friendly reads of public teams. {@code SecurityConfig}
 *       permits both authenticated and anonymous access; the authenticated user (when
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

    /**
     * The only properties a team listing may be sorted by. Anything else - in particular a path
     * through {@code owner} - is dropped; see {@link SortWhitelist}.
     */
    private static final Set<String> SORTABLE_PROPERTIES = Set.of("id", "name", "likeCount", "createdAt", "updatedAt");

    private final TeamQueryService teamQueryService;
    private final TeamCommandService teamCommandService;

    /** Public catalog of teams. */
    @PostMapping("/public/filter")
    public ResponseEntity<Page<TeamSummaryDto>> getPublicTeams(@AuthenticationPrincipal AuthenticatedUser user, @PageableDefault(page = 0, size = 20, sort = "likeCount", direction = Direction.DESC) Pageable pageable, @Valid @RequestBody TeamFilterDto filter) {
        Pageable safePageable = SortWhitelist.restrict(pageable, SORTABLE_PROPERTIES, Sort.by(Direction.DESC, "likeCount"));
        Page<TeamSummaryDto> summaries = teamQueryService.filterPublicSummaries(filter, safePageable, userIdOrNull(user));
        return ResponseEntity.ok(summaries);
    }

    /** Single public team read. */
    @PostMapping("/public/id")
    public ResponseEntity<TeamReadDto> getPublicTeamById(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody TeamFilterDto filter) {
        TeamReadDto team = teamQueryService.findPublicTeamById(filter.getId(), userIdOrNull(user));
        return ResponseEntity.ok(team);
    }

    /** Read-only view of a team, public or private, through its share link. Anonymous-friendly. */
    @GetMapping("/shared/{slug}")
    public ResponseEntity<TeamReadDto> getSharedTeam(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String slug) {
        TeamReadDto team = teamQueryService.findSharedTeam(slug, userIdOrNull(user));
        return ResponseEntity.ok(team);
    }

    /** Authenticated user's own teams (public + private). */
    @PostMapping("/me/filter")
    public ResponseEntity<Page<TeamSummaryDto>> getMyTeams(@AuthenticationPrincipal AuthenticatedUser user, @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Direction.DESC) Pageable pageable, @Valid @RequestBody TeamFilterDto filter) {
        Pageable safePageable = SortWhitelist.restrict(pageable, SORTABLE_PROPERTIES, Sort.by(Direction.DESC, "createdAt"));
        Page<TeamSummaryDto> summaries = teamQueryService.filterOwnedSummaries(filter, safePageable, user.getId());
        return ResponseEntity.ok(summaries);
    }

    /** Single owned-team read. 404s if the team isn't owned by the caller. */
    @PostMapping("/me/id")
    public ResponseEntity<TeamReadDto> getMyTeamById(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody TeamFilterDto filter) {
        TeamReadDto team = teamQueryService.findOwnedTeamById(filter.getId(), user.getId());
        return ResponseEntity.ok(team);
    }

    /** Creates a team owned by the authenticated user. */
    @PostMapping
    public ResponseEntity<TeamReadDto> createTeam(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody TeamCreateDto dto) {
        Long createdTeamId = teamCommandService.createTeam(user.getId(), dto);
        TeamReadDto createdTeam = teamQueryService.findById(createdTeamId, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
    }

    /** Full replacement of an owned team. */
    @PutMapping("/{id}")
    public ResponseEntity<TeamReadDto> updateTeam(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id, @Valid @RequestBody TeamUpdateDto dto) {
        Long updatedTeamId = teamCommandService.updateTeam(user.getId(), id, dto);
        TeamReadDto updatedTeam = teamQueryService.findById(updatedTeamId, user.getId());
        return ResponseEntity.ok(updatedTeam);
    }

    /** Partial update of an owned team (metadata only). */
    @PatchMapping("/{id}")
    public ResponseEntity<TeamReadDto> patchTeam(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id, @Valid @RequestBody TeamPatchDto dto) {
        Long patchedTeamId = teamCommandService.patchTeam(user.getId(), id, dto);
        TeamReadDto patchedTeam = teamQueryService.findById(patchedTeamId, user.getId());
        return ResponseEntity.ok(patchedTeam);
    }

    /** Issues a new share link for an owned team; links handed out before stop working. */
    @PostMapping("/{id}/share-link/rotate")
    public ResponseEntity<TeamReadDto> rotateShareLink(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        Long teamId = teamCommandService.rotateShareSlug(user.getId(), id);
        TeamReadDto team = teamQueryService.findById(teamId, user.getId());
        return ResponseEntity.ok(team);
    }

    /** Deletes an owned team. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        teamCommandService.deleteTeam(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    /** Likes a public team. */
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeTeam(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        teamCommandService.likeTeam(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    /** Removes the caller's like from a team. */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikeTeam(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        teamCommandService.unlikeTeam(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    /** Admin: delete any team. */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDeleteTeam(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        teamCommandService.adminDeleteTeam(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    private Long userIdOrNull(AuthenticatedUser user) {
        return user == null ? null : user.getId();
    }
}
