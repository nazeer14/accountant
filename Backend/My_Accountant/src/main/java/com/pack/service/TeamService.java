package com.pack.service;

import com.pack.dto.request.RoleChangeRequestDto;
import com.pack.dto.request.TeamInviteAcceptDto;
import com.pack.dto.request.TeamMemberRequestDto;
import com.pack.dto.request.TeamRequestDto;
import com.pack.dto.response.*;
import com.pack.enums.TeamRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TeamService {

    // ── Team CRUD ─────────────────────────────────────────────────────────────

    /** Create a new team and auto-add owner as OWNER role */
    TeamResponseDto create(TeamRequestDto dto);

    /** Get full team details by ID */
    TeamResponseDto getById(UUID teamId);

    /** Paginated list of all teams (admin) */
    Page<TeamResponseDto> getAll(Pageable pageable);

    /** Paginated list of teams owned by a user */
    Page<TeamResponseDto> getByOwnerId(UUID ownerId, Pageable pageable);

    /** Search teams by name keyword */
    Page<TeamResponseDto> searchByName(String keyword, Pageable pageable);

    /** Full update of team metadata */
    TeamResponseDto update(UUID teamId, TeamRequestDto dto);

    /** Soft toggle: activate or deactivate a team */
    TeamResponseDto toggleActive(UUID teamId, Boolean active);

    /** Hard delete a team and all its memberships */
    void delete(UUID teamId);

    // ── Invite Token ──────────────────────────────────────────────────────────

    /** Generate (or regenerate) a unique invite token for the team */
    TeamInviteResponseDto generateInviteToken(UUID teamId);

    /** Revoke / invalidate the existing invite token */
    void revokeInviteToken(UUID teamId);

    /** Lookup team by invite token (for preview before joining) */
    TeamResponseDto getByInviteToken(String token);

    /** Accept an invite and join the team as MEMBER */
    TeamMemberResponseDto acceptInvite(TeamInviteAcceptDto dto);

    // ── Members ───────────────────────────────────────────────────────────────

    /** Directly add a member with a specific role (admin action) */
    TeamMemberResponseDto addMember(UUID teamId, TeamMemberRequestDto dto);

    /** Get paginated member list for a team */
    Page<TeamMemberResponseDto> getMembers(UUID teamId, Pageable pageable);

    /** Get all teams a user belongs to */
    Page<TeamMemberResponseDto> getTeamsForUser(UUID userId, Pageable pageable);

    /** Get members of a specific role in a team */
    List<TeamMemberResponseDto> getMembersByRole(UUID teamId, TeamRole role);

    /** Change the role of a member */
    TeamMemberResponseDto changeRole(UUID teamId, UUID userId, RoleChangeRequestDto dto);

    /** Remove a member from the team */
    void removeMember(UUID teamId, UUID userId);

    /** Owner transfers ownership to another member */
    TeamResponseDto transferOwnership(UUID teamId, UUID newOwnerId);

    // ── Analytics ─────────────────────────────────────────────────────────────

    /** Aggregated stats (member counts by role etc.) for a team */
    TeamSummaryDto getSummary(UUID teamId);
}