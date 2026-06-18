package com.pack.service.impl;

import com.pack.dto.request.RoleChangeRequestDto;
import com.pack.dto.request.TeamInviteAcceptDto;
import com.pack.dto.request.TeamMemberRequestDto;
import com.pack.dto.request.TeamRequestDto;
import com.pack.dto.response.*;
import com.pack.entity.Team;
import com.pack.entity.TeamMember;
import com.pack.entity.User;
import com.pack.enums.TeamRole;
import com.pack.exceptions.*;
import com.pack.mapper.TeamMapper;
import com.pack.repository.TeamMemberRepository;
import com.pack.repository.TeamRepository;
import com.pack.repository.UserRepository;
import com.pack.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    private static final int TOKEN_LENGTH  = 32;
    private static final int MAX_TOKEN_RETRIES = 5;
    private static final String TOKEN_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Value("${app.invite.base-url:http://localhost:8080/api/v1/teams/join}")
    private String inviteBaseUrl;

    private final TeamRepository       teamRepository;
    private final TeamMemberRepository memberRepository;
    private final UserRepository       userRepository;
    private final TeamMapper           teamMapper;
    private final SecureRandom         secureRandom = new SecureRandom();

    // ─── Team CRUD ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TeamResponseDto create(TeamRequestDto dto) {
        log.info("Creating team '{}' for owner: {}", dto.teamName(), dto.ownerId());

        User owner = findUserOrThrow(dto.ownerId());

        if (teamRepository.existsByTeamNameIgnoreCaseAndOwnerId(dto.teamName(), dto.ownerId())) {
            throw new BusinessException(
                    String.format("Owner already has a team named '%s'", dto.teamName()));
        }

        Team team = teamMapper.toEntity(dto);
        team.setOwner(owner);
        team = teamRepository.save(team);

        // Auto-enroll owner as OWNER role
        TeamMember ownerMember = TeamMember.builder()
                .team(team)
                .user(owner)
                .role(TeamRole.OWNER)
                .build();
        memberRepository.save(ownerMember);

        log.info("Team created with id: {}", team.getId());
        return enrichWithMemberCount(team);
    }

    @Override
    public TeamResponseDto getById(UUID teamId) {
        Team team = findTeamOrThrow(teamId);
        return enrichWithMemberCount(team);
    }

    @Override
    public Page<TeamResponseDto> getAll(Pageable pageable) {
        return teamRepository.findAll(pageable).map(this::enrichWithMemberCount);
    }

    @Override
    public Page<TeamResponseDto> getByOwnerId(UUID ownerId, Pageable pageable) {
        if (!userRepository.existsById(ownerId)) {
            throw new ResourceNotFoundException("User", ownerId);
        }
        return teamRepository.findByOwnerId(ownerId, pageable).map(this::enrichWithMemberCount);
    }

    @Override
    public Page<TeamResponseDto> searchByName(String keyword, Pageable pageable) {
        return teamRepository.searchByTeamName(keyword, pageable).map(this::enrichWithMemberCount);
    }

    @Override
    @Transactional
    public TeamResponseDto update(UUID teamId, TeamRequestDto dto) {
        log.info("Updating team: {}", teamId);
        Team team = findTeamOrThrow(teamId);
        assertTeamActive(team);

        // Prevent rename collision
        if (!team.getTeamName().equalsIgnoreCase(dto.teamName())
                && teamRepository.existsByTeamNameIgnoreCaseAndOwnerId(dto.teamName(), dto.ownerId())) {
            throw new BusinessException(
                    String.format("Owner already has a team named '%s'", dto.teamName()));
        }

        teamMapper.updateEntityFromDto(dto, team);
        return enrichWithMemberCount(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamResponseDto toggleActive(UUID teamId, Boolean active) {
        log.info("Setting team {} active={}", teamId, active);
        Team team = findTeamOrThrow(teamId);
        teamRepository.updateActiveStatus(teamId, active);
        team.setActive(active);
        return enrichWithMemberCount(team);
    }

    @Override
    @Transactional
    public void delete(UUID teamId) {
        log.warn("Deleting team and all memberships: {}", teamId);
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", teamId);
        }
        memberRepository.deleteAllByTeamId(teamId);
        teamRepository.deleteById(teamId);
    }

    // ─── Invite Token ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TeamInviteResponseDto generateInviteToken(UUID teamId) {
        Team team = findTeamOrThrow(teamId);
        assertTeamActive(team);

        String token = generateUniqueToken();
        teamRepository.updateInviteToken(teamId, token);
        team.setInviteToken(token);

        log.info("Invite token generated for team: {}", teamId);
        return new TeamInviteResponseDto(
                team.getId(),
                team.getTeamName(),
                token,
                inviteBaseUrl + "?token=" + token
        );
    }

    @Override
    @Transactional
    public void revokeInviteToken(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", teamId);
        }
        teamRepository.revokeInviteToken(teamId);
        log.info("Invite token revoked for team: {}", teamId);
    }

    @Override
    public TeamResponseDto getByInviteToken(String token) {
        Team team = teamRepository.findByInviteTokenWithOwner(token)
                .orElseThrow(InvalidInviteTokenException::new);
        return enrichWithMemberCount(team);
    }

    @Override
    @Transactional
    public TeamMemberResponseDto acceptInvite(TeamInviteAcceptDto dto) {
        log.info("User {} accepting invite with token: {}", dto.userId(), dto.inviteToken());

        Team team = teamRepository.findByInviteTokenWithOwner(dto.inviteToken())
                .orElseThrow(InvalidInviteTokenException::new);
        assertTeamActive(team);

        User user = findUserOrThrow(dto.userId());

        if (memberRepository.existsByTeamIdAndUserId(team.getId(), user.getId())) {
            throw new DuplicateMemberException(user.getId(), team.getId());
        }

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(TeamRole.MEMBER)
                .build();

        return teamMapper.toMemberResponseDto(memberRepository.save(member));
    }

    // ─── Members ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TeamMemberResponseDto addMember(UUID teamId, TeamMemberRequestDto dto) {
        log.info("Adding user {} to team {} with role {}", dto.userId(), teamId, dto.role());

        Team team = findTeamOrThrow(teamId);
        assertTeamActive(team);

        // Prevent OWNER role assignment via this endpoint
        if (dto.role() == TeamRole.OWNER) {
            throw new BusinessException("OWNER role cannot be assigned directly. Use transferOwnership instead.");
        }

        User user = findUserOrThrow(dto.userId());

        if (memberRepository.existsByTeamIdAndUserId(teamId, user.getId())) {
            throw new DuplicateMemberException(user.getId(), teamId);
        }

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(dto.role())
                .build();

        return teamMapper.toMemberResponseDto(memberRepository.save(member));
    }

    @Override
    public Page<TeamMemberResponseDto> getMembers(UUID teamId, Pageable pageable) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", teamId);
        }
        return memberRepository.findByTeamIdWithUser(teamId, pageable)
                .map(teamMapper::toMemberResponseDto);
    }

    @Override
    public Page<TeamMemberResponseDto> getTeamsForUser(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return memberRepository.findByUserIdWithTeam(userId, pageable)
                .map(teamMapper::toMemberResponseDto);
    }

    @Override
    public List<TeamMemberResponseDto> getMembersByRole(UUID teamId, TeamRole role) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", teamId);
        }
        return memberRepository.findByTeamIdAndRole(teamId, role)
                .stream()
                .map(teamMapper::toMemberResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public TeamMemberResponseDto changeRole(UUID teamId, UUID userId, RoleChangeRequestDto dto) {
        log.info("Changing role of user {} in team {} to {}", userId, teamId, dto.newRole());

        if (dto.newRole() == TeamRole.OWNER) {
            throw new BusinessException("Use transferOwnership to assign OWNER role.");
        }

        TeamMember member = memberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember", userId));

        // Cannot demote or change the current OWNER via this route
        if (member.getRole() == TeamRole.OWNER) {
            throw new BusinessException("Cannot change the role of the current team owner. Use transferOwnership.");
        }

        memberRepository.updateRole(teamId, userId, dto.newRole());
        member.setRole(dto.newRole());
        return teamMapper.toMemberResponseDto(member);
    }

    @Override
    @Transactional
    public void removeMember(UUID teamId, UUID userId) {
        log.info("Removing user {} from team {}", userId, teamId);

        TeamMember member = memberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember", teamId, userId));

        if (member.getRole() == TeamRole.OWNER) {
            throw new BusinessException("Team owner cannot be removed. Transfer ownership first.");
        }

        memberRepository.deleteByTeamIdAndUserId(teamId, userId);
    }

    @Override
    @Transactional
    public TeamResponseDto transferOwnership(UUID teamId, UUID newOwnerId) {
        log.info("Transferring ownership of team {} to user {}", teamId, newOwnerId);

        Team team = findTeamOrThrow(teamId);
        assertTeamActive(team);

        User newOwner = findUserOrThrow(newOwnerId);

        // Verify new owner is an existing member
        TeamMember newOwnerMember = memberRepository.findByTeamIdAndUserId(teamId, newOwnerId)
                .orElseThrow(() -> new BusinessException(
                        "The new owner must already be a member of the team."));

        // Demote current owner to ADMIN
        UUID currentOwnerId = team.getOwner().getId();
        memberRepository.updateRole(teamId, currentOwnerId, TeamRole.ADMIN);

        // Promote new owner
        memberRepository.updateRole(teamId, newOwnerId, TeamRole.OWNER);
        newOwnerMember.setRole(TeamRole.OWNER);

        // Update team's owner reference
        team.setOwner(newOwner);
        Team saved = teamRepository.save(team);

        log.info("Ownership of team {} transferred from {} to {}", teamId, currentOwnerId, newOwnerId);
        return enrichWithMemberCount(saved);
    }

    // ─── Analytics ────────────────────────────────────────────────────────────

    @Override
    public TeamSummaryDto getSummary(UUID teamId) {
        Team team = findTeamOrThrow(teamId);

        List<Object[]> rawCounts = memberRepository.countGroupedByRoleForTeam(teamId);
        Map<TeamRole, Long> byRole = rawCounts.stream()
                .collect(Collectors.toMap(
                        row -> (TeamRole) row[0],
                        row -> (Long)     row[1]
                ));

        long total = byRole.values().stream().mapToLong(Long::longValue).sum();

        return new TeamSummaryDto(
                team.getId(),
                team.getTeamName(),
                total,
                byRole,
                team.getActive()
        );
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private Team findTeamOrThrow(UUID teamId) {
        return teamRepository.findByIdWithOwner(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private void assertTeamActive(Team team) {
        if (Boolean.FALSE.equals(team.getActive())) {
            throw new TeamInactiveException(team.getId());
        }
    }

    private TeamResponseDto enrichWithMemberCount(Team team) {
        long count = memberRepository.countByTeamId(team.getId());
        TeamResponseDto base = teamMapper.toResponseDto(team);
        // Re-construct with member count since record is immutable
        return new TeamResponseDto(
                base.id(), base.ownerId(),
                base.teamName(), base.inviteToken(), base.active(),
                count, base.createdAt(), base.updatedAt()
        );
    }

    private String generateUniqueToken() {
        for (int attempt = 0; attempt < MAX_TOKEN_RETRIES; attempt++) {
            String token = buildRandomToken(TOKEN_LENGTH);
            if (!teamRepository.existsByInviteToken(token)) {
                return token;
            }
        }
        throw new BusinessException("Failed to generate a unique invite token. Please try again.");
    }

    private String buildRandomToken(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(TOKEN_CHARS.charAt(secureRandom.nextInt(TOKEN_CHARS.length())));
        }
        return sb.toString();
    }
}