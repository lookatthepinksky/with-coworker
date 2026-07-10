package com.devksg.withcoworkers.repository;

import com.devksg.withcoworkers.domain.TeamMember;
import com.devksg.withcoworkers.domain.TeamMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    boolean existsByTeamIdAndUserIdAndStatus(Long teamId, Long userId, TeamMemberStatus status);

    boolean existsByUserId(Long userId);

    boolean existsByUserIdAndStatus(Long userId, TeamMemberStatus status);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.team WHERE tm.user.id = :userId AND tm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.APPROVED")
    Optional<TeamMember> findByUserId(@Param("userId") Long userId);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user WHERE tm.team.id = :teamId AND tm.user.id <> :userId AND tm.status = com.devksg.withcoworkers.domain.TeamMemberStatus.APPROVED")
    List<TeamMember> findByTeamIdAndUserIdNot(@Param("teamId") Long teamId, @Param("userId") Long userId);

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
