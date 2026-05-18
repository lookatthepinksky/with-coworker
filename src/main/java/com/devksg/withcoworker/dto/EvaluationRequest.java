package com.devksg.withcoworker.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class EvaluationRequest {

    private Long evaluateeId;
    private String targetMonth;
    private String comment;
    private List<ScoreItem> scores;

    @Getter
    public static class ScoreItem {
        private Long itemId;
        private int score;
    }
}
