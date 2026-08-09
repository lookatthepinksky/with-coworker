package com.devksg.withcoworkers.controller;

import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.dto.ResultSummaryResponse;
import com.devksg.withcoworkers.dto.ScoreTrendResponse;
import com.devksg.withcoworkers.repository.EvaluationRepository;
import com.devksg.withcoworkers.repository.EvaluationScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/result")
@RequiredArgsConstructor
public class ResultController {

    private final EvaluationScoreRepository evaluationScoreRepository;
    private final EvaluationRepository evaluationRepository;

    @GetMapping("/monthly-scores")
    public ResponseEntity<ScoreTrendResponse> getTrend(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") int months
    ) {
        LocalDate endMonth = YearMonth.now().minusMonths(1).atDay(1);
        LocalDate startMonth = YearMonth.now().minusMonths(months).atDay(1);

        List<Object[]> rows = evaluationScoreRepository.findMonthlyAvgByEvaluateeAndRange(
                user.getId(), startMonth, endMonth
        );

        Map<LocalDate, List<Object[]>> byMonth = rows.stream()
                .collect(Collectors.groupingBy(row -> (LocalDate) row[0]));

        List<ScoreTrendResponse.MonthPoint> points = byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<ScoreTrendResponse.ItemScore> scores = entry.getValue().stream()
                            .map(row -> ScoreTrendResponse.ItemScore.builder()
                                    .label((String) row[1])
                                    .score(Math.round((Double) row[2] * 10.0) / 10.0)
                                    .build())
                            .toList();
                    return ScoreTrendResponse.MonthPoint.builder()
                            .month(entry.getKey().format(DateTimeFormatter.ofPattern("yyyy-MM")))
                            .scores(scores)
                            .build();
                })
                .toList();

        return ResponseEntity.ok(ScoreTrendResponse.builder().data(points).build());
    }

    @GetMapping("/summary")
    public ResponseEntity<ResultSummaryResponse> getSummary(
            @AuthenticationPrincipal User user,      // 현재 로그인한 사용자 (피평가자)
            @RequestParam(defaultValue = "1") int months  // 조회할 기간 (월 수, 기본값 1개월)
    ) {
        int n = months;

        // 현재 기간: (n개월 전 1일) ~ (지난달 1일)
        // 예) months=1 이면 "지난달 1개월", months=3 이면 "최근 3개월"
        LocalDate currentEnd = YearMonth.now().minusMonths(1).atDay(1);     // 현재 기간 끝 (지난달 1일)
        LocalDate currentStart = YearMonth.now().minusMonths(n).atDay(1);   // 현재 기간 시작 (n개월 전 1일)

        // 비교 기간: 현재 기간 바로 이전의 동일한 길이 구간
        // 예) months=1 이면 현재 기간보다 한 달 앞, months=3 이면 3달 앞
        LocalDate prevEnd = YearMonth.now().minusMonths(n + 1).atDay(1);    // 이전 기간 끝
        LocalDate prevStart = YearMonth.now().minusMonths(2 * n).atDay(1);  // 이전 기간 시작

        // 현재 기간의 평가 항목별 평균 점수 조회 (row: [항목명, 평균점수])
        List<Object[]> currentRows = evaluationScoreRepository
                .findPeriodAvgByEvaluateeAndRange(user.getId(), currentStart, currentEnd);

        // 이전 기간의 평가 항목별 평균 점수 조회 (증감 비교용)
        List<Object[]> prevRows = evaluationScoreRepository
                .findPeriodAvgByEvaluateeAndRange(user.getId(), prevStart, prevEnd);

        // 이전 기간 점수를 항목명 기준으로 Map화 → 현재 점수와 빠르게 비교하기 위함
        Map<String, Double> prevMap = prevRows.stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Double) r[1]));

        // 현재 점수와 이전 점수를 항목별로 묶어 ScoreDto 리스트 생성
        List<ResultSummaryResponse.ScoreDto> scores = currentRows.stream()
                .map(r -> {
                    String label = (String) r[0];                                       // 평가 항목명
                    double current = Math.round((Double) r[1] * 10.0) / 10.0;          // 현재 점수 (소수점 1자리)
                    double prev = Math.round(prevMap.getOrDefault(label, 0.0) * 10.0) / 10.0; // 이전 점수 (없으면 0.0)
                    return ResultSummaryResponse.ScoreDto.builder()
                            .label(label).current(current).prev(prev).build();
                })
                .toList();

        // 현재 기간에 작성된 코멘트 조회 (row: [코멘트 내용, 작성 월])
        List<Object[]> commentRows = evaluationRepository
                .findCommentsByEvaluateeAndRange(user.getId(), currentStart, currentEnd);

        // 코멘트 내용과 작성 월(yyyy.MM 형식)을 CommentDto로 변환
        List<ResultSummaryResponse.CommentDto> comments = commentRows.stream()
                .map(r -> ResultSummaryResponse.CommentDto.builder()
                        .text((String) r[0])
                        .month(((LocalDate) r[1]).format(DateTimeFormatter.ofPattern("yyyy.MM")))
                        .build())
                .toList();

        // 현재 기간 동안 나를 평가한 평가자 수 집계
        long totalEvaluations = evaluationRepository
                .countEvaluatorsByEvaluateeAndRange(user.getId(), currentStart, currentEnd);
        double avgEvaluatorCount = Double.parseDouble(String.format("%.1f", (double) totalEvaluations / n));

        // 기간 문자열, 월별 평가자 수, 점수 목록, 코멘트 목록을 담아 응답 반환
        return ResponseEntity.ok(ResultSummaryResponse.builder()
                .period(buildPeriodString(currentStart, currentEnd))// 언제 부터 언제까지의 평가인지
                .evaluatorCount(avgEvaluatorCount) //해당 기간의 월별 평가자 평균 수
                .scores(scores) // 레이더차트에 들어가는 값
                .comments(comments) //주관식
                .build());
    }

    private String buildPeriodString(LocalDate start, LocalDate end) {
        if (start.equals(end)) {
            return start.getYear() + "년 " + start.getMonthValue() + "월";
        }
        if (start.getYear() == end.getYear()) {
            return start.getYear() + "년 " + start.getMonthValue() + "월 ~ " + end.getMonthValue() + "월";
        }
        return start.getYear() + "년 " + start.getMonthValue() + "월 ~ "
                + end.getYear() + "년 " + end.getMonthValue() + "월";
    }
}
