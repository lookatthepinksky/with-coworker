package com.devksg.withcoworkers.service;

import com.devksg.withcoworkers.domain.*;
import com.devksg.withcoworkers.dto.PendingMemberResponse;
import com.devksg.withcoworkers.repository.TeamMemberRepository;
import com.devksg.withcoworkers.repository.TeamRepository;
import com.devksg.withcoworkers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public List<Map<String, Object>> getTeams() {
        return teamRepository.findAll().stream()
                .map(t -> Map.<String, Object>of("teamId", t.getId(), "name", t.getName()))
                .toList();
    }

    @Transactional
    public void createTeam(String name, Long userId) {
        if (teamMemberRepository.existsByUserId(userId)) {
            throw new IllegalStateException("이미 팀에 속해 있습니다.");
        }
        User user = userRepository.findById(userId).orElseThrow();
        Team team = teamRepository.save(Team.builder().name(name).build());
        teamMemberRepository.save(TeamMember.builder()
                .team(team).user(user)
                .role(TeamMemberRole.ADMIN).status(TeamMemberStatus.APPROVED)
                .build());
    }

    @Transactional
    public void joinTeam(Long teamId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, user.getId())) {
            throw new IllegalStateException("이미 팀에 속해 있습니다.");
        }

        teamMemberRepository.save(TeamMember.builder()
                .team(team).user(user)
                .role(TeamMemberRole.MEMBER).status(TeamMemberStatus.PENDING)
                .build());
    }

    @Transactional(readOnly = true)
    public List<PendingMemberResponse> getPendingMembers(Long userId) {
        return teamMemberRepository.findAdminMembershipByUserId(userId)
                .map(admin -> teamMemberRepository.findPendingByTeamId(admin.getTeam().getId())
                        .stream()
                        .map(tm -> new PendingMemberResponse(
                                tm.getId(), tm.getUser().getName(), tm.getUser().getEmail()))
                        .toList())
                .orElse(List.of());
    }

    @Transactional
    public void approveMember(Long teamMemberId, Long adminUserId) {
        int updated = teamMemberRepository.approveIfPendingAndSameTeam(teamMemberId, adminUserId);
        if (updated == 0) throw new IllegalStateException("권한이 없거나 대기 중인 멤버가 아닙니다.");
    }

    @Transactional
    public void rejectMember(Long teamMemberId, Long adminUserId) {
        int deleted = teamMemberRepository.deleteIfPendingAndSameTeam(teamMemberId, adminUserId);
        if (deleted == 0) throw new IllegalStateException("권한이 없거나 대기 중인 멤버가 아닙니다.");
    }
}
