package com.devksg.withcoworkers.service;

import com.devksg.withcoworkers.domain.TeamMember;
import com.devksg.withcoworkers.domain.TeamMemberRole;
import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.dto.DashboardResponse;
import com.devksg.withcoworkers.repository.EvaluationRepository;
import com.devksg.withcoworkers.repository.EvaluationScoreRepository;
import com.devksg.withcoworkers.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final EvaluationRepository evaluationRepository;
    private final EvaluationScoreRepository evaluationScoreRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User me, String month) {
        LocalDate targetMonth = YearMonth.parse(month, MONTH_FORMATTER).atDay(1);

        // findByUserId now returns only APPROVED memberships
        TeamMember myMembership = teamMemberRepository.findByUserId(me.getId()).orElse(null);

        // check for pending membership if no approved one exists
        TeamMember pendingMembership = myMembership == null
                ? teamMemberRepository.findPendingMembershipByUserId(me.getId()).orElse(null)
                : null;
        boolean isPending = pendingMembership != null;

        String teamName = myMembership != null ? myMembership.getTeam().getName()
                : (pendingMembership != null ? pendingMembership.getTeam().getName() : null);
        boolean isAdmin = myMembership != null && myMembership.getRole() == TeamMemberRole.ADMIN;

        // findByTeamIdAndUserIdNot now returns only APPROVED teammates
        List<User> others = myMembership == null
                ? Collections.emptyList()
                : teamMemberRepository
                .findByTeamIdAndUserIdNot(myMembership.getTeam().getId(), me.getId())
                .stream()
                .map(TeamMember::getUser)
                .toList();

        Set<Long> evaluatedIds = new HashSet<>(
                evaluationRepository.findEvaluateeIdsByEvaluatorAndTargetMonth(me, targetMonth));

        List<DashboardResponse.TeammateDto> teammates = others.stream()
                .map(u -> DashboardResponse.TeammateDto.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .done(evaluatedIds.contains(u.getId()))
                        .build())
                .toList();

        List<DashboardResponse.ScoreDto> myScores = evaluationScoreRepository
                .findAvgScoresByEvaluateeIdAndTargetMonth(me.getId(), targetMonth).stream()
                .map(row -> DashboardResponse.ScoreDto.builder()
                        .label((String) row[0])
                        .score(Math.round((Double) row[1] * 10.0) / 10.0)
                        .build())
                .toList();

        return DashboardResponse.builder()
                .userName(me.getName())
                .teamName(teamName)
                .isAdmin(isAdmin)
                .isPending(isPending)
                .teammates(teammates)
                .myScores(myScores)
                .build();
    }
}
