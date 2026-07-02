package com.devksg.withcoworkers.controller;

import com.devksg.withcoworkers.service.VisitorStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visitor")
@RequiredArgsConstructor
public class VisitorStatisticsController {

    private final VisitorStatisticsService visitorStatisticsService;

    @PostMapping
    public ResponseEntity<Void> recordVisit() {
        visitorStatisticsService.recordVisit();
        return ResponseEntity.ok().build();
    }
}
