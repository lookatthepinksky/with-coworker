package com.devksg.withcoworker.repository;

import com.devksg.withcoworker.domain.Evaluation;
import com.devksg.withcoworker.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    boolean existsByEvaluatorAndEvaluateeAndCreatedAtBetween(
        User evaluator, User evaluatee, LocalDateTime start, LocalDateTime end);

    @Query("SELECT e.evaluatee.id FROM Evaluation e " +
        "WHERE e.evaluator = :evaluator AND e.createdAt BETWEEN :start AND :end")
    List<Long> findEvaluateeIdsByEvaluatorAndCreatedAtBetween(
        @Param("evaluator") User evaluator,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    List<Evaluation> findByEvaluatee(User evaluatee);
}
