package com.devksg.withcoworker.repository;

import com.devksg.withcoworker.domain.EvaluationScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EvaluationScoreRepository extends JpaRepository<EvaluationScore, Long> {

    @Query("""
        SELECT es.item.label, AVG(es.score)
        FROM EvaluationScore es
        WHERE es.evaluation.evaluatee.id = :userId
        GROUP BY es.item.id, es.item.label
        ORDER BY es.item.id
    """)
    List<Object[]> findAvgScoresByEvaluateeId(@Param("userId") Long userId);
}
