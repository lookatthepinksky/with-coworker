package com.devksg.withcoworker.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationIdempotencyService {

    private static final Duration TTL = Duration.ofDays(35);
    private static final DateTimeFormatter YEAR_MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final StringRedisTemplate redisTemplate;

    public boolean isAlreadySent(EmailMessage message) {
        String key = buildKey(message);
        try {
            Boolean exists = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(exists)) {
                log.info("[Redis중복감지] key={}", key);
                return true;
            }
            return false;
        } catch (Exception e) {
            // Redis 장애 시 발송을 막지 않고 DB 체크로 fallback
            log.warn("[Redis조회실패] key={} error={}", key, e.getMessage());
            return false;
        }
    }

    public void markAsSent(EmailMessage message) {
        String key = buildKey(message);
        try {
            redisTemplate.opsForValue().set(key, "1", TTL);
            log.debug("[Redis멱등성키설정] key={} ttl=35d", key);
        } catch (Exception e) {
            // 마킹 실패는 치명적이지 않음 — DB 기록이 2차 보호 역할
            log.warn("[Redis마킹실패] key={} error={}", key, e.getMessage());
        }
    }

    // notification:sent:{email}:{notificationType}:{yearMonth}
    private String buildKey(EmailMessage message) {
        String yearMonth = YearMonth.of(message.evaluationYear(), message.evaluationMonth())
                .format(YEAR_MONTH_FMT);
        return "notification:sent:" + message.userEmail() + ":" + message.type().toDbType() + ":" + yearMonth;
    }
}
