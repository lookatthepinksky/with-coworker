package com.devksg.withcoworker.controller;

import com.devksg.withcoworker.domain.User;
import com.devksg.withcoworker.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getTeams() {
        return ResponseEntity.ok(teamService.getTeams());
    }

    @PostMapping("/{teamId}/join")
    public ResponseEntity<Void> joinTeam(@PathVariable Long teamId,
                                         @AuthenticationPrincipal User user) {
        teamService.joinTeam(teamId, user.getId());
        return ResponseEntity.ok().build();
    }
}
