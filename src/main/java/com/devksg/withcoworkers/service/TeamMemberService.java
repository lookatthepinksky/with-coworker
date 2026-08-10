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

    @Transactional(readOnly = true)
    public TeamMemberOverviewResponse getTeamMembersOverview(User me, String targetMonth) {
        LocalDate target = YearMonth.parse(targetMonth, MONTH_FORMATTER).atDay(1);

        TeamMember myMembership = teamMemberRepository.findByUserId(me.getId()).orElse(null);

        TeamMember pendingMembership = myMembership == null
                ? teamMemberRepository.findPendingMembershipByUserId(me.getId()).orElse(null)
                : null;
        boolean isPending = pendingMembership != null;

        String teamName = myMembership != null ? myMembership.getTeam().getName()
                : (pendingMembership != null ? pendingMembership.getTeam().getName() : null);
        boolean isAdmin = myMembership != null && myMembership.getRole() == TeamMemberRole.ADMIN;

        List<TeamMemberOverviewResponse.TeammateDto> teammates = myMembership == null
                ? Collections.emptyList()
                : teamMemberRepository
                .findTeammatesWithEvaluationStatus(myMembership.getTeam().getId(), me.getId(), me, target)
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
