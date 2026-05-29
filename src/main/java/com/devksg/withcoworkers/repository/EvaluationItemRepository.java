package com.devksg.withcoworkers.repository;

import com.devksg.withcoworkers.domain.EvaluationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationItemRepository extends JpaRepository<EvaluationItem, Long> {
}
