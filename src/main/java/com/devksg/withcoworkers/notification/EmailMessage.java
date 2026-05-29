package com.devksg.withcoworkers.notification;

/**
 * SQS 큐에 넣을 이메일 메시지 DTO
 * evaluationYear/Month = 평가 대상 월 (e.g. 2026년 4월 평가)
 */
public record EmailMessage(
        EmailNotificationType type,
        String userEmail,
        String userName,
        int evaluationYear,
        int evaluationMonth,
        Long authProviderId  // auth_providers.id, null 허용 (Google 전용 계정 등)
) {}
