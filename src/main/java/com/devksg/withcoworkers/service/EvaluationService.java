package com.devksg.withcoworker.service;

import com.devksg.withcoworker.domain.Evaluation;
import com.devksg.withcoworker.domain.EvaluationItem;
import com.devksg.withcoworker.domain.EvaluationScore;
import com.devksg.withcoworker.domain.User;
import com.devksg.withcoworker.dto.EvaluationRequest;
import com.devksg.withcoworker.repository.EvaluationItemRepository;
import com.devksg.withcoworker.repository.EvaluationRepository;
import com.devksg.withcoworker.repository.EvaluationScoreRepository;
import com.devksg.withcoworker.repository.TeamMemberRepository;
import com.devksg.withcoworker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final EvaluationRepository evaluationRepository;
    private final EvaluationScoreRepository evaluationScoreRepository;
    private final EvaluationItemRepository evaluationItemRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

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

        Evaluation evaluation = evaluationRepository.save(
            Evaluation.builder()
                .evaluator(evaluator)
                .evaluatee(evaluatee)
                .comment(request.getComment())
                .targetMonth(targetMonth)
                .build()
        );

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
