package com.devksg.withcoworker.service;

import com.devksg.withcoworker.domain.Team;
import com.devksg.withcoworker.domain.TeamMember;
import com.devksg.withcoworker.domain.User;
import com.devksg.withcoworker.repository.TeamMemberRepository;
import com.devksg.withcoworker.repository.TeamRepository;
import com.devksg.withcoworker.repository.UserRepository;
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
    public void joinTeam(Long teamId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, user.getId())) {
            throw new IllegalStateException("이미 팀에 속해 있습니다.");
        }

        teamMemberRepository.save(TeamMember.builder().team(team).user(user).build());
    }
}
