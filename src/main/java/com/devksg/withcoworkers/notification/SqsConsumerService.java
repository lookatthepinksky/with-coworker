package com.devksg.withcoworkers.notification;

import com.devksg.withcoworkers.config.SqsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

@Service
@Slf4j
public class SqsConsumerService {

    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final EvaluationEmailService emailService;
    private final EmailRateLimiter emailRateLimiter;
    private final NotificationIdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;
    private final TaskExecutor emailTaskExecutor;

    @Autowired
    public SqsConsumerService(SqsClient sqsClient,
                              SqsProperties sqsProperties,
                              EvaluationEmailService emailService,
                              EmailRateLimiter emailRateLimiter,
                              NotificationIdempotencyService idempotencyService,
                              ObjectMapper objectMapper,
                              @Qualifier("emailTaskExecutor") TaskExecutor emailTaskExecutor) {
        this.sqsClient = sqsClient;
        this.sqsProperties = sqsProperties;
        this.emailService = emailService;
        this.emailRateLimiter = emailRateLimiter;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
        this.emailTaskExecutor = emailTaskExecutor;
    }

    private volatile String queueUrl;

    /**
     * SQS 메시지 폴링 - 5초 간격, long-poll(20초) 방식으로 효율적 처리
     * 수신된 메시지는 emailTaskExecutor 스레드 풀에서 비동기 병렬 처리
     */
    //@Scheduled(fixedDelay = 5000)
    public void pollAndProcess() {
        List<Message> messages;
        try {
            messages = sqsClient.receiveMessage(r -> r
                    .queueUrl(getQueueUrl())
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(20)
            ).messages();
        } catch (Exception e) {
            log.warn("[SQS폴링실패] {}", e.getMessage());
            return;
        }

        if (messages.isEmpty()) return;

        log.info("[SQS수신] {}건 처리 시작", messages.size());
        messages.forEach(message ->
                emailTaskExecutor.execute(() -> processMessage(message))
        );
    }

    private void processMessage(Message message) {
        try {
            EmailMessage emailMessage = objectMapper.readValue(message.body(), EmailMessage.class);

            // 1차 중복 체크: Redis 멱등성 키 (빠른 경로, at-least-once SQS 중복 방지)
            if (idempotencyService.isAlreadySent(emailMessage)) {
                log.info("[Redis중복스킵] type={} to={}", emailMessage.type(), emailMessage.userEmail());
                deleteMessage(message.receiptHandle());
                return;
            }

            // 2차 중복 체크: DB 이력 (Redis 미설정 누락 시 fallback)
            if (emailRateLimiter.isDuplicate(emailMessage)) {
                log.info("[DB중복스킵] type={} to={}", emailMessage.type(), emailMessage.userEmail());
                deleteMessage(message.receiptHandle());
                return;
            }

            // 일일 한도 체크: 500건 초과 시 메시지 삭제하지 않고 보류 (내일 재처리)
            if (!emailRateLimiter.tryConsume()) {
                return;
            }

            emailService.sendEmail(emailMessage);
            idempotencyService.markAsSent(emailMessage);
            deleteMessage(message.receiptHandle());
        } catch (Exception e) {
            log.error("[메시지처리실패] messageId={} error={}", message.messageId(), e.getMessage());
            // 삭제하지 않음 → SQS visibility timeout 경과 후 자동 재처리
        }
    }

    private void deleteMessage(String receiptHandle) {
        sqsClient.deleteMessage(r -> r
                .queueUrl(getQueueUrl())
                .receiptHandle(receiptHandle));
    }

    private String getQueueUrl() {
        if (queueUrl == null) {
            synchronized (this) {
                if (queueUrl == null) {
                    queueUrl = sqsClient
                            .getQueueUrl(r -> r.queueName(sqsProperties.getQueueName()))
                            .queueUrl();
                    log.info("[SQS] Consumer Queue URL 초기화: {}", queueUrl);
                }
            }
        }
        return queueUrl;
    }
}
