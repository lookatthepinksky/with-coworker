package com.devksg.withcoworkers.notification;

import com.devksg.withcoworkers.repository.NotificationHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class EmailRateLimiter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final int dailyLimit;
    private final StringRedisTemplate redisTemplate;
    private final NotificationHistoryRepository historyRepository;

    public EmailRateLimiter(
            @Value("${notification.email.daily-limit}") int dailyLimit,
            StringRedisTemplate redisTemplate,
            NotificationHistoryRepository historyRepository) {
        this.dailyLimit = dailyLimit;
        this.redisTemplate = redisTemplate;
        this.historyRepository = historyRepository;
    }

    /**
     * 발송 가능 여부 확인 + Redis 카운터 증가.
     * false 반환 시 오늘 한도 초과 → SQS 메시지 보류 (내일 재처리)
     */
    public boolean tryConsume() {
        String key = "notification:daily-count:" + LocalDate.now().format(DATE_FMT);
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == null) {
                log.warn("[일일한도체크실패] Redis INCR 결과 null - 발송 허용(fail-open)");
                return true;
            }
            if (count == 1L) {
                redisTemplate.expire(key, Duration.ofDays(2));
            }
            if (count > dailyLimit) {
                redisTemplate.opsForValue().decrement(key);
                log.warn("[일일한도초과] 금일 발송 한도 {}건 도달 - 발송 보류", dailyLimit);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("[일일한도체크실패] key={} error={} - 발송 허용(fail-open)", key, e.getMessage());
            return true;
        }
    }

    /** 이번 달 동일 타입으로 이미 발송 성공한 이력이 있으면 true (중복 방지) */
    public boolean isDuplicate(EmailMessage message) {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return historyRepository.existsSuccessThisMonth(
                message.userEmail(),
                message.type().toDbType(),
                monthStart
        );
    }
}
