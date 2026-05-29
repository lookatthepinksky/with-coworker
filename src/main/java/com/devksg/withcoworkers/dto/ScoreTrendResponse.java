package com.devksg.withcoworkers.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ScoreTrendResponse {
    private List<MonthPoint> data;

    @Getter
    @Builder
    public static class MonthPoint {
        private String month;
        private List<ItemScore> scores;
    }

    @Getter
    @Builder
    public static class ItemScore {
        private String label;
        private double score;
    }
}
