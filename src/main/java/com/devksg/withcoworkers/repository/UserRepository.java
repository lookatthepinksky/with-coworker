package com.devksg.withcoworkers.repository;

import com.devksg.withcoworkers.domain.TeamMemberStatus;
import com.devksg.withcoworkers.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("""
        SELECT tm2.user.name
        FROM TeamMember tm1
        JOIN TeamMember tm2 ON tm1.team = tm2.team
        WHERE tm1.user.id = :evaluatorId
          AND tm1.status = :status
          AND tm2.user.id = :targetId
          AND tm2.status = :status
          AND NOT EXISTS (
                SELECT e
                FROM Evaluation e
                WHERE e.evaluator.id = :evaluatorId
                  AND e.targetMonth = :targetMonth
                  AND e.evaluatee = tm2.user
              )
    """)
    Optional<String> findEvaluatableTargetName(
        @Param("evaluatorId") Long evaluatorId,
        @Param("targetId") Long targetId,
        @Param("targetMonth") LocalDate targetMonth,
        @Param("status") TeamMemberStatus status
    );
}
