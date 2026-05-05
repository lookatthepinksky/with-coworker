package com.devksg.withcoworker.service;

import com.devksg.withcoworker.domain.TeamMember;
import com.devksg.withcoworker.domain.User;
import com.devksg.withcoworker.dto.DashboardResponse;
import com.devksg.withcoworker.repository.EvaluationRepository;
import com.devksg.withcoworker.repository.EvaluationScoreRepository;
import com.devksg.withcoworker.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationScoreRepository evaluationScoreRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User me) {
        YearMonth thisMonth = YearMonth.now();
        LocalDateTime start = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime end = thisMonth.atEndOfMonth().atTime(23, 59, 59);

        TeamMember myMembership = teamMemberRepository.findByUserId(me.getId()).orElse(null);
        String teamName = myMembership != null ? myMembership.getTeam().getName() : null;

        List<User> others = myMembership == null
            ? Collections.emptyList()
            : teamMemberRepository
                .findByTeamIdAndUserIdNot(myMembership.getTeam().getId(), me.getId())
                .stream()
                .map(TeamMember::getUser)
                .toList();

        Set<Long> evaluatedIds = new HashSet<>(
            evaluationRepository.findEvaluateeIdsByEvaluatorAndCreatedAtBetween(me, start, end));

        List<DashboardResponse.TeammateDto> teammates = others.stream()
            .map(u -> DashboardResponse.TeammateDto.builder()
                .id(u.getId())
                .name(u.getName())
                .done(evaluatedIds.contains(u.getId()))
                .build())
            .toList();

        List<DashboardResponse.ScoreDto> myScores = evaluationScoreRepository
            .findAvgScoresByEvaluateeId(me.getId()).stream()
            .map(row -> DashboardResponse.ScoreDto.builder()
                .label((String) row[0])
                .score(Math.round((Double) row[1] * 10.0) / 10.0)
                .build())
            .toList();

        return DashboardResponse.builder()
            .userName(me.getName())
            .teamName(teamName)
            .teammates(teammates)
            .myScores(myScores)
            .build();
    }
}
