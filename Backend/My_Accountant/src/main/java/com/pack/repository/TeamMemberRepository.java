package com.pack.repository;

import com.pack.entity.TeamMember;
import com.pack.enums.TeamRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    // ─── Join-fetch safe lookups ──────────────────────────────────────────────

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user JOIN FETCH tm.team WHERE tm.id = :id")
    Optional<TeamMember> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user WHERE tm.team.id = :teamId")
    Page<TeamMember> findByTeamIdWithUser(@Param("teamId") UUID teamId, Pageable pageable);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.team WHERE tm.user.id = :userId")
    Page<TeamMember> findByUserIdWithTeam(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user WHERE tm.team.id = :teamId AND tm.role = :role")
    List<TeamMember> findByTeamIdAndRole(@Param("teamId") UUID teamId, @Param("role") TeamRole role);

    // ─── Membership checks ────────────────────────────────────────────────────

    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);

    @Query("SELECT tm.role FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.user.id = :userId")
    Optional<TeamRole> findRoleByTeamIdAndUserId(@Param("teamId") UUID teamId, @Param("userId") UUID userId);

    // ─── Counts ───────────────────────────────────────────────────────────────

    long countByTeamId(UUID teamId);

    long countByTeamIdAndRole(UUID teamId, TeamRole role);

    long countByUserId(UUID userId);

    // ─── Aggregation by role ──────────────────────────────────────────────────

    @Query("SELECT tm.role, COUNT(tm) FROM TeamMember tm WHERE tm.team.id = :teamId GROUP BY tm.role")
    List<Object[]> countGroupedByRoleForTeam(@Param("teamId") UUID teamId);

    // ─── Role change ──────────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE TeamMember tm SET tm.role = :role WHERE tm.team.id = :teamId AND tm.user.id = :userId")
    int updateRole(@Param("teamId") UUID teamId, @Param("userId") UUID userId, @Param("role") TeamRole role);

    // ─── Removal ──────────────────────────────────────────────────────────────

    @Modifying
    @Query("DELETE FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.user.id = :userId")
    void deleteByTeamIdAndUserId(@Param("teamId") UUID teamId, @Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM TeamMember tm WHERE tm.team.id = :teamId")
    void deleteAllByTeamId(@Param("teamId") UUID teamId);

    // ─── Team ID list for a user ──────────────────────────────────────────────

    @Query("SELECT tm.team.id FROM TeamMember tm WHERE tm.user.id = :userId")
    List<UUID> findTeamIdsByUserId(@Param("userId") UUID userId);
}