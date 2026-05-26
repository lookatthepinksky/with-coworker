package com.devksg.withcoworker.repository;

import com.devksg.withcoworker.domain.EvaluationScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EvaluationScoreRepository extends JpaRepository<EvaluationScore, Long> {

    @Query("""
        SELECT es.item.label, AVG(es.score)
        FROM EvaluationScore es
        WHERE es.evaluation.evaluatee.id = :userId
          AND es.evaluation.targetMonth = :targetMonth
        GROUP BY es.item.id, es.item.label
        ORDER BY es.item.id
    """)
    List<Object[]> findAvgScoresByEvaluateeIdAndTargetMonth(
        @Param("userId") Long userId,
        @Param("targetMonth") LocalDate targetMonth
    );

    @Query("""
        SELECT es.evaluation.targetMonth, es.item.label, AVG(es.score)
        FROM EvaluationScore es
        WHERE es.evaluation.evaluatee.id = :userId
          AND es.evaluation.targetMonth >= :startMonth
          AND es.evaluation.targetMonth <= :endMonth
        GROUP BY es.evaluation.targetMonth, es.item.id, es.item.label
        ORDER BY es.evaluation.targetMonth, es.item.id
    """)
    List<Object[]> findMonthlyAvgByEvaluateeAndRange(
        @Param("userId") Long userId,
        @Param("startMonth") LocalDate startMonth,
        @Param("endMonth") LocalDate endMonth
    );

    @Query("""
        SELECT es.item.label, AVG(es.score)
        FROM EvaluationScore es
        WHERE es.evaluation.evaluatee.id = :userId
          AND es.evaluation.targetMonth >= :startMonth
          AND es.evaluation.targetMonth <= :endMonth
        GROUP BY es.item.id, es.item.label
        ORDER BY es.item.id
    """)
    List<Object[]> findPeriodAvgByEvaluateeAndRange(
        @Param("userId") Long userId,
        @Param("startMonth") LocalDate startMonth,
        @Param("endMonth") LocalDate endMonth
    );
}
