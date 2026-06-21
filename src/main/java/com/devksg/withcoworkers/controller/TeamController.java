package com.devksg.withcoworkers.controller;

import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.dto.PendingMemberResponse;
import com.devksg.withcoworkers.service.TeamService;
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

    @PostMapping
    public ResponseEntity<Void> createTeam(@RequestBody Map<String, String> body,
                                           @AuthenticationPrincipal User user) {
        teamService.createTeam(body.get("name"), user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{teamId}/join")
    public ResponseEntity<Void> joinTeam(@PathVariable Long teamId,
                                         @AuthenticationPrincipal User user) {
        teamService.joinTeam(teamId, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PendingMemberResponse>> getPendingMembers(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(teamService.getPendingMembers(user.getId()));
    }

    @PostMapping("/members/{teamMemberId}/approve")
    public ResponseEntity<Void> approveMember(@PathVariable Long teamMemberId,
                                              @AuthenticationPrincipal User user) {
        teamService.approveMember(teamMemberId, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/members/{teamMemberId}")
    public ResponseEntity<Void> rejectMember(@PathVariable Long teamMemberId,
                                             @AuthenticationPrincipal User user) {
        teamService.rejectMember(teamMemberId, user.getId());
        return ResponseEntity.ok().build();
    }
}
