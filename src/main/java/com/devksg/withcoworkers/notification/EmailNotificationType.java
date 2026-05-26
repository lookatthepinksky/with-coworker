package com.devksg.withcoworker.notification;

public enum EmailNotificationType {
    START,    // 매월 1일 - 평가 시작 안내
    REMINDER, // 매월 5일 - 마감 2일 전 안내
    DEADLINE; // 매월 7일 - 마감 당일 안내

    public String toDbType() {
        return switch (this) {
            case START -> "CAMPAIGN_START";
            case REMINDER -> "DEADLINE_D2";
            case DEADLINE -> "DEADLINE_DDAY";
        };
    }
}
