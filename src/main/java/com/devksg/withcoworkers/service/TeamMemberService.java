package com.devksg.withcoworkers.service;

import com.devksg.withcoworkers.domain.TeamMember;
import com.devksg.withcoworkers.domain.TeamMemberRole;
import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.dto.TeamMemberOverviewResponse;
import com.devksg.withcoworkers.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final TeamMemberRepository teamMemberRepository;
    private final UserTeamCacheService userTeamCacheService;

    @Transactional(readOnly = true)
    public TeamMemberOverviewResponse getTeamMembersOverview(User me, String targetMonth) {
        LocalDate target = YearMonth.parse(targetMonth, MONTH_FORMATTER).atDay(1);

        // Redis에서 teamId/teamName/isAdmin 조회 → 있으면 findByUserId DB 쿼리 생략
        var cached = userTeamCacheService.getUserInfo(me.getId()).orElse(null);
        Long teamId = null;
        String teamName = null;
        boolean isAdmin = false;

        if (cached != null && cached.containsKey("teamId")) {
            teamId = Long.parseLong((String) cached.get("teamId"));
            teamName = (String) cached.get("teamName");
            isAdmin = Boolean.parseBoolean((String) cached.get("isAdmin"));
        } else {
            TeamMember myMembership = teamMemberRepository.findByUserId(me.getId()).orElse(null);
            if (myMembership != null) {
                teamId = myMembership.getTeam().getId();
                teamName = myMembership.getTeam().getName();
                isAdmin = myMembership.getRole() == TeamMemberRole.ADMIN;
            }
        }

        boolean isPending = false;
        if (teamId == null) {
            TeamMember pendingMembership = teamMemberRepository.findPendingMembershipByUserId(me.getId()).orElse(null);
            isPending = pendingMembership != null;
            if (pendingMembership != null) {
                teamName = pendingMembership.getTeam().getName();
            }
        }

        List<TeamMemberOverviewResponse.TeammateDto> teammates = teamId == null
                ? Collections.emptyList()
                : teamMemberRepository
                .findTeammatesWithEvaluationStatus(teamId, me.getId(), me, target)
                .stream()
                .map(row -> TeamMemberOverviewResponse.TeammateDto.builder()
                        .id((Long) row[0])
                        .name((String) row[1])
                        .done((Boolean) row[2])
                        .build())
                .toList();

        return TeamMemberOverviewResponse.builder()
                .userName(me.getName())
                .teamName(teamName)
                .isAdmin(isAdmin)
                .isPending(isPending)
                .teammates(teammates)
                .build();
    }
}
