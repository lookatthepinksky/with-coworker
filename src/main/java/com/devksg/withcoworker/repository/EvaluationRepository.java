package com.devksg.withcoworker.repository;

import com.devksg.withcoworker.domain.Evaluation;
import com.devksg.withcoworker.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    boolean existsByEvaluatorAndEvaluateeAndTargetMonth(
        User evaluator, User evaluatee, LocalDate targetMonth);

    @Query("SELECT e.evaluatee.id FROM Evaluation e " +
        "WHERE e.evaluator = :evaluator AND e.targetMonth = :targetMonth")
    List<Long> findEvaluateeIdsByEvaluatorAndTargetMonth(
        @Param("evaluator") User evaluator,
        @Param("targetMonth") LocalDate targetMonth);

    List<Evaluation> findByEvaluatee(User evaluatee);

    @Query("""
        SELECT e.comment, e.targetMonth
        FROM Evaluation e
        WHERE e.evaluatee.id = :userId
          AND e.targetMonth >= :startMonth
          AND e.targetMonth <= :endMonth
          AND e.comment IS NOT NULL
          AND TRIM(e.comment) <> ''
        ORDER BY e.targetMonth DESC
    """)
    List<Object[]> findCommentsByEvaluateeAndRange(
        @Param("userId") Long userId,
        @Param("startMonth") LocalDate startMonth,
        @Param("endMonth") LocalDate endMonth
    );

    @Query("""
        SELECT COUNT(DISTINCT e.evaluator.id)
        FROM Evaluation e
        WHERE e.evaluatee.id = :userId
          AND e.targetMonth >= :startMonth
          AND e.targetMonth <= :endMonth
    """)
    long countEvaluatorsByEvaluateeAndRange(
        @Param("userId") Long userId,
        @Param("startMonth") LocalDate startMonth,
        @Param("endMonth") LocalDate endMonth
    );
}
