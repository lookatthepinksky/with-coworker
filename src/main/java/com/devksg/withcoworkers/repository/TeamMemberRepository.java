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
    Optional<TeamMember> findByUserId(@Param("userId") Long userId); //Optional 은 결과가 0개 또는 1개

    //<>는 != 라는 뜻. 해당쿼리는 아마도 나를 제외한 팀원들 목록을 조회 할 때 쓰는거 같음
    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user WHERE tm.team.id = :teamId AND tm.user.id <> :userId")
    List<TeamMember> findByTeamIdAndUserIdNot(@Param("teamId") Long teamId, @Param("userId") Long userId); //List는 결과가 0개 또는 여러개일때
}
