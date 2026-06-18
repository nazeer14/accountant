package com.pack.controller;

import com.pack.dto.request.RoleChangeRequestDto;
import com.pack.dto.request.TeamInviteAcceptDto;
import com.pack.dto.request.TeamMemberRequestDto;
import com.pack.dto.request.TeamRequestDto;
import com.pack.dto.response.*;
import com.pack.enums.TeamRole;
import com.pack.exceptions.ErrorResponse;
import com.pack.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Team management and membership APIs")
public class TeamController {

    private static final int MAX_PAGE_SIZE = 100;

    private final TeamService teamService;

    // ═══════════════════════════════════════════════════════════════════════════
    //  TEAM CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping
    @Operation(summary = "Create a new team",
            description = "Creates a team and automatically enrolls the owner as OWNER role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Team created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Owner not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Team name already in use by this owner")
    })
    public ResponseEntity<ApiResponse<TeamResponseDto>> create(
            @Valid @RequestBody TeamRequestDto dto) {
        log.info("POST /api/v1/teams - owner: {}", dto.ownerId());
        TeamResponseDto created = teamService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Team created successfully"));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "Get team by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<ApiResponse<TeamResponseDto>> getById(
            @Parameter(description = "Team UUID") @PathVariable UUID teamId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getById(teamId)));
    }

    @GetMapping
    @Operation(summary = "Get all teams (admin)", description = "Paginated list of all teams.")
    public ResponseEntity<ApiResponse<Page<TeamResponseDto>>> getAll(
            @RequestParam(defaultValue = "0")  @Min(0)        int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt")          String sortBy,
            @RequestParam(defaultValue = "DESC")               Sort.Direction direction) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.getAll(buildPageable(page, size, sortBy, direction))));
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get teams by owner")
    public ResponseEntity<ApiResponse<Page<TeamResponseDto>>> getByOwner(
            @PathVariable UUID ownerId,
            @RequestParam(defaultValue = "0")  @Min(0)              int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)    int size,
            @RequestParam(defaultValue = "createdAt")               String sortBy,
            @RequestParam(defaultValue = "DESC")                    Sort.Direction direction) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.getByOwnerId(ownerId, buildPageable(page, size, sortBy, direction))));
    }

    @GetMapping("/search")
    @Operation(summary = "Search teams by name keyword")
    public ResponseEntity<ApiResponse<Page<TeamResponseDto>>> search(
            @RequestParam @NotBlank String keyword,
            @RequestParam(defaultValue = "0")  @Min(0)              int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)    int size) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.searchByName(keyword, buildPageable(page, size, "teamName", Sort.Direction.ASC))));
    }

    @PutMapping("/{teamId}")
    @Operation(summary = "Update team metadata (full update)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Team is inactive or name conflict")
    })
    public ResponseEntity<ApiResponse<TeamResponseDto>> update(
            @PathVariable UUID teamId,
            @Valid @RequestBody TeamRequestDto dto) {
        log.info("PUT /api/v1/teams/{}", teamId);
        return ResponseEntity.ok(ApiResponse.success(
                teamService.update(teamId, dto), "Team updated successfully"));
    }

    @PatchMapping("/{teamId}/active")
    @Operation(summary = "Activate or deactivate a team")
    public ResponseEntity<ApiResponse<TeamResponseDto>> toggleActive(
            @PathVariable UUID teamId,
            @RequestParam Boolean active) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.toggleActive(teamId, active),
                active ? "Team activated" : "Team deactivated"));
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "Delete a team and all its memberships")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Team deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID teamId) {
        log.warn("DELETE /api/v1/teams/{}", teamId);
        teamService.delete(teamId);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  INVITE TOKEN
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/{teamId}/invite/generate")
    @Operation(summary = "Generate (or regenerate) an invite token",
            description = "Creates a cryptographically random token. If one already exists it is replaced.")
    public ResponseEntity<ApiResponse<TeamInviteResponseDto>> generateInvite(
            @PathVariable UUID teamId) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.generateInviteToken(teamId), "Invite token generated"));
    }

    @DeleteMapping("/{teamId}/invite/revoke")
    @Operation(summary = "Revoke the current invite token",
            description = "Invalidates the invite link immediately. Existing members are not affected.")
    public ResponseEntity<ApiResponse<Void>> revokeInvite(@PathVariable UUID teamId) {
        teamService.revokeInviteToken(teamId);
        return ResponseEntity.ok(ApiResponse.success(null, "Invite token revoked"));
    }

    @GetMapping("/join/preview")
    @Operation(summary = "Preview team info from invite token (before joining)")
    public ResponseEntity<ApiResponse<TeamResponseDto>> previewInvite(
            @RequestParam @NotBlank String token) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getByInviteToken(token)));
    }

    @PostMapping("/join")
    @Operation(summary = "Accept a team invite and join as MEMBER")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Joined team"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Already a member"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Team is inactive")
    })
    public ResponseEntity<ApiResponse<TeamMemberResponseDto>> acceptInvite(
            @Valid @RequestBody TeamInviteAcceptDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(teamService.acceptInvite(dto), "Joined team successfully"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  MEMBERS
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/{teamId}/members")
    @Operation(summary = "Add a member directly (admin action)",
            description = "Adds a user with a specified role. OWNER cannot be assigned here.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Member added"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already a member")
    })
    public ResponseEntity<ApiResponse<TeamMemberResponseDto>> addMember(
            @PathVariable UUID teamId,
            @Valid @RequestBody TeamMemberRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(teamService.addMember(teamId, dto), "Member added"));
    }

    @GetMapping("/{teamId}/members")
    @Operation(summary = "List all members of a team (paginated)")
    public ResponseEntity<ApiResponse<Page<TeamMemberResponseDto>>> getMembers(
            @PathVariable UUID teamId,
            @RequestParam(defaultValue = "0")  @Min(0)              int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)    int size,
            @RequestParam(defaultValue = "createdAt")               String sortBy,
            @RequestParam(defaultValue = "ASC")                     Sort.Direction direction) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.getMembers(teamId, buildPageable(page, size, sortBy, direction))));
    }

    @GetMapping("/{teamId}/members/by-role")
    @Operation(summary = "Get members filtered by role")
    public ResponseEntity<ApiResponse<List<TeamMemberResponseDto>>> getMembersByRole(
            @PathVariable UUID teamId,
            @RequestParam TeamRole role) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getMembersByRole(teamId, role)));
    }

    @GetMapping("/users/{userId}/memberships")
    @Operation(summary = "Get all team memberships for a user")
    public ResponseEntity<ApiResponse<Page<TeamMemberResponseDto>>> getMembershipsForUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0")  @Min(0)              int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)    int size) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.getTeamsForUser(userId, buildPageable(page, size, "createdAt", Sort.Direction.DESC))));
    }

    @PatchMapping("/{teamId}/members/{userId}/role")
    @Operation(summary = "Change a member's role",
            description = "Cannot be used to assign or remove OWNER. Use /transfer-ownership for that.")
    public ResponseEntity<ApiResponse<TeamMemberResponseDto>> changeRole(
            @PathVariable UUID teamId,
            @PathVariable UUID userId,
            @Valid @RequestBody RoleChangeRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.changeRole(teamId, userId, dto), "Role updated successfully"));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @Operation(summary = "Remove a member from the team",
            description = "The owner cannot be removed — transfer ownership first.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Member removed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Cannot remove the owner")
    })
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID teamId,
            @PathVariable UUID userId) {
        teamService.removeMember(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/transfer-ownership")
    @Operation(summary = "Transfer team ownership",
            description = "Promotes a member to OWNER and demotes the current owner to ADMIN.")
    public ResponseEntity<ApiResponse<TeamResponseDto>> transferOwnership(
            @PathVariable UUID teamId,
            @RequestParam UUID newOwnerId) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.transferOwnership(teamId, newOwnerId),
                "Ownership transferred successfully"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  ANALYTICS
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/{teamId}/summary")
    @Operation(summary = "Get aggregated team statistics")
    public ResponseEntity<ApiResponse<TeamSummaryDto>> getSummary(@PathVariable UUID teamId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getSummary(teamId)));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private org.springframework.data.domain.Pageable buildPageable(
            int page, int size, String sortBy, Sort.Direction direction) {
        return PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), Sort.by(direction, sortBy));
    }
}