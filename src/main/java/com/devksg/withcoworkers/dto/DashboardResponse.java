package com.devksg.withcoworkers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponse {
    private String userName;
    private String teamName;
    @JsonProperty("isAdmin")
    private boolean isAdmin;
    @JsonProperty("isPending")
    private boolean isPending;
    private List<TeammateDto> teammates;

    @Getter
    @Builder
    public static class TeammateDto {
        private Long id;
        private String name;
        private boolean done;
    }
}
