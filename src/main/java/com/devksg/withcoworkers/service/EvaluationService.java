package com.devksg.withcoworkers.service;

import com.devksg.withcoworkers.domain.Evaluation;
import com.devksg.withcoworkers.domain.EvaluationItem;
import com.devksg.withcoworkers.domain.EvaluationScore;
import com.devksg.withcoworkers.domain.TeamMemberStatus;
import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.dto.EvaluationRequest;
import com.devksg.withcoworkers.repository.EvaluationRepository;
import com.devksg.withcoworkers.repository.EvaluationScoreRepository;
import com.devksg.withcoworkers.repository.TeamMemberRepository;
import com.devksg.withcoworkers.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final EvaluationRepository evaluationRepository;
    private final EvaluationScoreRepository evaluationScoreRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final EntityManager em;
    private final UserRepository userRepository;
    private final UserTeamCacheService userTeamCacheService;

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

        Long myTeamId = userTeamCacheService.getTeamId(evaluator.getId())
                .orElseThrow(() -> new IllegalStateException("팀 정보를 찾을 수 없습니다."));

        if (!teamMemberRepository.existsValidEvaluatee(myTeamId, evaluatee.getId(), evaluator.getId())) {
            throw new IllegalStateException("잘못된 피평가자입니다.");
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

        List<EvaluationScore> scores = request.getScores().stream()
                .map(scoreItem -> EvaluationScore.builder() //스트림의 .map(scoreItem -> ...) 이 for문의 for (ScoreItem scoreItem : ...) 랑 같은 역할
                        .evaluation(evaluation)
                        .item(em.getReference(EvaluationItem.class, scoreItem.getItemId())) //em.getReference <- EntiryManager의 메서드. DB조회 없이 프록시 객체를 만들어 줌
                        .score(scoreItem.getScore())
                        .build())
                .toList();
        evaluationScoreRepository.saveAll(scores);
    }
}
