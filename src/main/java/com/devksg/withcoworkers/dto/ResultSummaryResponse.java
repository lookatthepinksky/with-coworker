package com.devksg.withcoworkers.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResultSummaryResponse {
    private String period;
    private long evaluatorCount;
    private List<ScoreDto> scores;
    private List<CommentDto> comments;

    @Getter
    @Builder
    public static class ScoreDto {
        private String label;
        private double current;
        private double prev;
    }

    @Getter
    @Builder
    public static class CommentDto {
        private String text;
        private String month;
    }
}
