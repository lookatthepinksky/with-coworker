package com.devksg.withcoworkers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingMemberResponse {
    private Long teamMemberId;
    private String name;
    private String email;
}
