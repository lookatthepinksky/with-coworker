package com.devksg.withcoworkers.controller;

import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.dto.TeamMemberOverviewResponse;
import com.devksg.withcoworkers.service.TeamMemberService;
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
@RequestMapping("/api/my/team-members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @GetMapping("/overview")
    public ResponseEntity<TeamMemberOverviewResponse> getTeamMembersOverview(
            @AuthenticationPrincipal User user,
            @RequestParam(name = "target_month", required = false) String targetMonth) {
        if (targetMonth == null) {
            targetMonth = YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        }
        return ResponseEntity.ok(teamMemberService.getTeamMembersOverview(user, targetMonth));
    }
}
