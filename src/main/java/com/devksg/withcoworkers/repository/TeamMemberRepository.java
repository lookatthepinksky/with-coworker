package com.devksg.withcoworkers.repository;

import com.devksg.withcoworkers.domain.TeamMember;
import com.devksg.withcoworkers.domain.TeamMemberStatus;
import com.devksg.withcoworkers.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    @Query("""
            SELECT CASE WHEN COUNT(tm) > 0 THEN true ELSE false END
            FROM TeamMember tm
            WHERE tm.team.id = :teamId
            AND tm.user.id = :evaluateeId
            AND tm.user.id != :evaluatorId
            AND tm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.APPROVED
            """)
    boolean existsValidEvaluatee(@Param("teamId") Long teamId,
                                 @Param("evaluateeId") Long evaluateeId,
                                 @Param("evaluatorId") Long evaluatorId);

    boolean existsByTeamIdAndUserIdAndStatus(Long teamId, Long userId, TeamMemberStatus status);

    boolean existsByUserId(Long userId);

    boolean existsByUserIdAndStatus(Long userId, TeamMemberStatus status);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.team WHERE tm.user.id = :userId AND tm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.APPROVED")
    Optional<TeamMember> findByUserId(@Param("userId") Long userId);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.team WHERE tm.user.id = :userId")
    Optional<TeamMember> findAnyMembershipByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT tm.user.id, tm.user.name,
                   CASE WHEN e IS NOT NULL THEN true ELSE false END
            FROM TeamMember tm
            LEFT JOIN Evaluation e
                ON e.evaluator = :evaluator
                AND e.evaluatee = tm.user
                AND e.targetMonth = :targetMonth
            WHERE tm.team.id = :teamId
            AND tm.user.id <> :myId
            AND tm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.APPROVED
            """)
    List<Object[]> findTeammatesWithEvaluationStatus(
            @Param("teamId") Long teamId,
            @Param("myId") Long myId,
            @Param("evaluator") User evaluator,
            @Param("targetMonth") LocalDate targetMonth);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user WHERE tm.team.id = :teamId AND tm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.PENDING")
    List<TeamMember> findPendingByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.team WHERE tm.user.id = :userId AND tm.role = com.devksg.withcoworkers.domain.TeamMemberRole.ADMIN AND tm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.APPROVED")
    Optional<TeamMember> findAdminMembershipByUserId(@Param("userId") Long userId);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.team WHERE tm.user.id = :userId AND tm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.PENDING")
    Optional<TeamMember> findPendingMembershipByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE TeamMember tm SET tm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.APPROVED " +
           "WHERE tm.id = :teamMemberId " +
           "AND tm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.PENDING " +
           "AND tm.team.id = (SELECT adm.team.id FROM TeamMember adm WHERE adm.user.id = :adminUserId AND adm.role = com.devksg.withcoworkers.domain.TeamMemberRole.ADMIN AND adm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.APPROVED)")
    int approveIfPendingAndSameTeam(@Param("teamMemberId") Long teamMemberId, @Param("adminUserId") Long adminUserId);

    @Modifying
    @Query("DELETE FROM TeamMember tm " +
           "WHERE tm.id = :teamMemberId " +
           "AND tm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.PENDING " +
           "AND tm.team.id = (SELECT adm.team.id FROM TeamMember adm WHERE adm.user.id = :adminUserId AND adm.role = com.devksg.withcoworkers.domain.TeamMemberRole.ADMIN AND adm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.APPROVED)")
    int deleteIfPendingAndSameTeam(@Param("teamMemberId") Long teamMemberId, @Param("adminUserId") Long adminUserId);
}
