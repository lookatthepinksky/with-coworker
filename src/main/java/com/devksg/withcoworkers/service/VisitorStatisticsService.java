package com.devksg.withcoworkers.service;

import com.devksg.withcoworkers.domain.VisitorStatistics;
import com.devksg.withcoworkers.repository.VisitorStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class VisitorStatisticsService {

    private final VisitorStatisticsRepository visitorStatisticsRepository;

    @Transactional
    public void recordVisit() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        VisitorStatistics stats = visitorStatisticsRepository.findById(today)
                .orElseGet(() -> visitorStatisticsRepository.save(new VisitorStatistics(today)));
        stats.increment();
    }
}
