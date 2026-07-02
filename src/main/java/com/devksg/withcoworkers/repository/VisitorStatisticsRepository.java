package com.devksg.withcoworkers.repository;

import com.devksg.withcoworkers.domain.VisitorStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface VisitorStatisticsRepository extends JpaRepository<VisitorStatistics, LocalDate> {
}
