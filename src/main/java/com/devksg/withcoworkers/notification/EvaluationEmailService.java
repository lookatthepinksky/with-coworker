package com.devksg.withcoworkers.notification;

import com.devksg.withcoworkers.domain.NotificationHistory;
import com.devksg.withcoworkers.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationEmailService {

    private final SesClient sesClient;
    private final NotificationHistoryRepository historyRepository;

    @Value("${ses.from-email}")
    private String fromEmail;

    private static final String SITE_URL = "https://with-coworker.vercel.app";

    public void sendEmail(EmailMessage message) {
        String subject = buildSubject(message);

        try {
            sesClient.sendEmail(SendEmailRequest.builder()
                    .source("with-coworkers <" + fromEmail + ">")
                    .destination(Destination.builder()
                            .toAddresses(message.userEmail())
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder()
                                    .text(Content.builder().data(buildContent(message)).charset("UTF-8").build())
                                    .build())
                            .build())
                    .build());

            log.info("[메일발송완료] type={} to={}", message.type(), message.userEmail());
            saveHistory(message, subject, "SUCCESS", null);

        } catch (Exception e) {
            log.error("[메일발송실패] type={} to={} error={}", message.type(), message.userEmail(), e.getMessage());
            saveHistory(message, subject, "FAIL", e.getMessage());
            throw new RuntimeException("메일 발송 실패: " + message.userEmail(), e);
        }
    }

    private void saveHistory(EmailMessage message, String subject, String status, String errorMessage) {
        try {
            historyRepository.save(NotificationHistory.builder()
                    .userId(message.userId())
                    .recipientEmail(message.userEmail())
                    .notificationType(message.type().toDbType())
                    .subject(subject)
                    .status(status)
                    .errorMessage(errorMessage)
                    .build());
        } catch (Exception e) {
            log.error("[이력저장실패] type={} to={} error={}", message.type(), message.userEmail(), e.getMessage());
        }
    }

    private String buildSubject(EmailMessage msg) {
        return switch (msg.type()) {
            case START -> "[동료평가] %d년 %02d월 평가를 시작해주세요.".formatted(
                    msg.evaluationYear(), msg.evaluationMonth());
            case REMINDER -> "[동료평가] 마감 2일 전 안내";
            case DEADLINE -> "[긴급][동료평가] 오늘 마감입니다.";
        };
    }

    private String buildContent(EmailMessage msg) {
        int deadlineMonth = (msg.evaluationMonth() % 12) + 1;
        int deadlineYear = (msg.evaluationMonth() == 12) ? msg.evaluationYear() + 1 : msg.evaluationYear();

        return switch (msg.type()) {
            case START -> """
                    안녕하세요. %s님,

                    %d년 %02d월 동료평가가 시작되었습니다.

                    마감일: %d년 %02d월 7일 (당일 23시 59분까지)

                    지금 평가하기: %s

                    감사합니다.
                    """.formatted(
                    msg.userName(),
                    msg.evaluationYear(), msg.evaluationMonth(),
                    deadlineYear, deadlineMonth,
                    SITE_URL);

            case REMINDER -> """
                    %s님,

                    동료평가 마감이 2일 남았습니다!

                    마감일: %d년 %02d월 7일

                    지금 평가하기: %s

                    감사합니다.
                    """.formatted(
                    msg.userName(),
                    deadlineYear, deadlineMonth,
                    SITE_URL);

            case DEADLINE -> """
                    %s님,

                    동료평가 마감이 오늘입니다.

                    마감 시각: 23시 59분

                    서둘러 완료해주세요: %s

                    감사합니다.
                    """.formatted(
                    msg.userName(),
                    SITE_URL);
        };
    }
}
