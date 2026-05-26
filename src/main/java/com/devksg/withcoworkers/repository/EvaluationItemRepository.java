package com.devksg.withcoworker.repository;

import com.devksg.withcoworker.domain.EvaluationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationItemRepository extends JpaRepository<EvaluationItem, Long> {
}
