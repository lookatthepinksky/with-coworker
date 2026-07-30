package com.devksg.withcoworkers.service;

import com.devksg.withcoworkers.domain.TeamMember;
import com.devksg.withcoworkers.domain.TeamMemberRole;
import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.dto.DashboardResponse;
import com.devksg.withcoworkers.repository.EvaluationRepository;
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
    private final TeamMemberRepository teamMemberRepository;

    //(readOnly = true) 스냅샷 저장하지 않아서 서버 메모리 사용량이 크게 줄고 변경감지 광정을 건너뛰기 때문에 조회 성능 빨라짐
    // 읽기 전용 db로 요청을 라우팅해줌 , 개발자가 실수로 데이터 수정 로직을 집어넣으면 예외 터트려주기때문에 데이터 변형 원천 차단 가능
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

        return DashboardResponse.builder()
                .userName(me.getName())
                .teamName(teamName)
                .isAdmin(isAdmin)
                .isPending(isPending)
                .teammates(teammates)
                .build();
    }
}
