package com.devksg.withcoworker.controller;

import com.devksg.withcoworker.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
                                         @AuthenticationPrincipal OAuth2User oAuth2User) {
        teamService.joinTeam(teamId, oAuth2User.getAttribute("sub"));
        return ResponseEntity.ok().build();
    }
}
