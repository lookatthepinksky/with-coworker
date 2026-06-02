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

    @GetMapping("/latest-evaluator-count")
    public ResponseEntity<Map<String, Long>> getLatestEvaluatorCount(@AuthenticationPrincipal User user) {
        long count = evaluationRepository.countEvaluatorsForLatestMonth(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/trend")
    public ResponseEntity<ScoreTrendResponse> getTrend(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "6") int months
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
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") int months
    ) {
        int n = months;

        LocalDate currentEnd = YearMonth.now().minusMonths(1).atDay(1);
        LocalDate currentStart = YearMonth.now().minusMonths(n).atDay(1);
        LocalDate prevEnd = YearMonth.now().minusMonths(n + 1).atDay(1);
        LocalDate prevStart = YearMonth.now().minusMonths(2 * n).atDay(1);

        List<Object[]> currentRows = evaluationScoreRepository
                .findPeriodAvgByEvaluateeAndRange(user.getId(), currentStart, currentEnd);
        List<Object[]> prevRows = evaluationScoreRepository
                .findPeriodAvgByEvaluateeAndRange(user.getId(), prevStart, prevEnd);

        Map<String, Double> prevMap = prevRows.stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Double) r[1]));

        List<ResultSummaryResponse.ScoreDto> scores = currentRows.stream()
                .map(r -> {
                    String label = (String) r[0];
                    double current = Math.round((Double) r[1] * 10.0) / 10.0;
                    double prev = Math.round(prevMap.getOrDefault(label, 0.0) * 10.0) / 10.0;
                    return ResultSummaryResponse.ScoreDto.builder()
                            .label(label).current(current).prev(prev).build();
                })
                .toList();

        List<Object[]> commentRows = evaluationRepository
                .findCommentsByEvaluateeAndRange(user.getId(), currentStart, currentEnd);
        List<ResultSummaryResponse.CommentDto> comments = commentRows.stream()
                .map(r -> ResultSummaryResponse.CommentDto.builder()
                        .text((String) r[0])
                        .month(((LocalDate) r[1]).format(DateTimeFormatter.ofPattern("yyyy.MM")))
                        .build())
                .toList();

        long evaluatorCount = evaluationRepository
                .countEvaluatorsByEvaluateeAndRange(user.getId(), currentStart, currentEnd);

        return ResponseEntity.ok(ResultSummaryResponse.builder()
                .period(buildPeriodString(currentStart, currentEnd))
                .evaluatorCount(evaluatorCount)
                .scores(scores)
                .comments(comments)
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
