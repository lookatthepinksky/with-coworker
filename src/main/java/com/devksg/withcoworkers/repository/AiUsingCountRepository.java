package com.devksg.withcoworkers.repository;

import com.devksg.withcoworkers.domain.AiUsingCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AiUsingCountRepository extends JpaRepository<AiUsingCount, Long> {
    Optional<AiUsingCount> findByEvaluatorIdAndEvaluateeIdAndTargetMonth(Long evaluatorId, Long evaluateeId, LocalDate targetMonth);
}
