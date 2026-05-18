package com.devksg.withcoworker.controller;

import com.devksg.withcoworker.domain.User;
import com.devksg.withcoworker.dto.DashboardResponse;
import com.devksg.withcoworker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String month) {
        if (month == null) {
            month = YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        }
        return ResponseEntity.ok(dashboardService.getDashboard(user, month));
    }
}
