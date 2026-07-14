package com.devksg.withcoworkers.service;

import com.devksg.withcoworkers.domain.Evaluation;
import com.devksg.withcoworkers.domain.EvaluationItem;
import com.devksg.withcoworkers.domain.EvaluationScore;
import com.devksg.withcoworkers.domain.TeamMemberStatus;
import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.dto.EvaluationRequest;
import com.devksg.withcoworkers.repository.EvaluationItemRepository;
import com.devksg.withcoworkers.repository.EvaluationRepository;
import com.devksg.withcoworkers.repository.EvaluationScoreRepository;
import com.devksg.withcoworkers.repository.TeamMemberRepository;
import com.devksg.withcoworkers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final EvaluationRepository evaluationRepository;
    private final EvaluationScoreRepository evaluationScoreRepository;
    private final EvaluationItemRepository evaluationItemRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public Map<String, String> getEvaluateTarget(User evaluator, Long targetId) {
        LocalDate targetMonth = YearMonth.now().minusMonths(1).atDay(1);
        String name = userRepository.findEvaluatableTargetName(evaluator.getId(), targetId, targetMonth,TeamMemberStatus.APPROVED)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 접근입니다."));
        return Map.of("name", name);
    }

    @Transactional
    public void submit(User evaluator, EvaluationRequest request) {
        LocalDate targetMonth = request.getTargetMonth() != null
            ? YearMonth.parse(request.getTargetMonth(), MONTH_FORMATTER).atDay(1)
            : YearMonth.now().minusMonths(1).atDay(1);

        User evaluatee = userRepository.findById(request.getEvaluateeId()).orElseThrow();

        if (evaluator.getId().equals(evaluatee.getId())) {
            throw new IllegalStateException("자기 자신은 평가할 수 없습니다.");
        }

        Long myTeamId = teamMemberRepository.findByUserId(evaluator.getId())
                .orElseThrow(() -> new IllegalStateException("팀에 소속되어 있지 않습니다."))
                .getTeam().getId();

        if (!teamMemberRepository.existsByTeamIdAndUserId(myTeamId, evaluatee.getId())) {
            throw new IllegalStateException("같은 팀 팀원만 평가할 수 있습니다.");
        }

        if (evaluationRepository.existsByEvaluatorAndEvaluateeAndTargetMonth(evaluator, evaluatee, targetMonth)) {
            throw new IllegalStateException("이미 해당 월에 평가를 완료했습니다.");
        }

        Evaluation evaluation;
        try {
            evaluation = evaluationRepository.save(
                Evaluation.builder()
                    .evaluator(evaluator)
                    .evaluatee(evaluatee)
                    .comment(request.getComment())
                    .targetMonth(targetMonth)
                    .build()
            );
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("이미 해당 월에 평가를 완료했습니다.");
        }

        for (EvaluationRequest.ScoreItem scoreItem : request.getScores()) {
            EvaluationItem item = evaluationItemRepository.findById(scoreItem.getItemId()).orElseThrow();
            evaluationScoreRepository.save(
                EvaluationScore.builder()
                    .evaluation(evaluation)
                    .item(item)
                    .score(scoreItem.getScore())
                    .build()
            );
        }
    }
}
