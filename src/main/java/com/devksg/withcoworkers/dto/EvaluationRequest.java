package com.devksg.withcoworkers.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class EvaluationRequest {

    private Long evaluateeId;
    private String targetMonth;

    @Size(min = 100, max = 130, message = "종합 의견은 100자 이상 130자 이하로 작성해주세요.")
    private String comment;

    private List<ScoreItem> scores;

    @Getter
    public static class ScoreItem {
        private Long itemId;
        private int score;
    }
}
