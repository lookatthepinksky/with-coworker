package com.devksg.withcoworker.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponse {
    private String userName;
    private String teamName;
    private List<TeammateDto> teammates;
    private List<ScoreDto> myScores;

    @Getter
    @Builder
    public static class TeammateDto {
        private Long id;
        private String name;
        private boolean done;
    }

    @Getter
    @Builder
    public static class ScoreDto {
        private String label;
        private double score;
    }
}
