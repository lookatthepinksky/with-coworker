package com.devksg.withcoworker.repository;

import com.devksg.withcoworker.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
    boolean existsByUserId(Long userId);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.team WHERE tm.user.id = :userId")
    Optional<TeamMember> findByUserId(@Param("userId") Long userId);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user WHERE tm.team.id = :teamId AND tm.user.id <> :userId")
    List<TeamMember> findByTeamIdAndUserIdNot(@Param("teamId") Long teamId, @Param("userId") Long userId);
}
