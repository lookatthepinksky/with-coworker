package com.devksg.withcoworkers.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String KEY_PREFIX = "login:fail:";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public void recordFailure(String loginId) {
        try {
            String key = KEY_PREFIX + loginId;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && (count == 1 || count >= MAX_ATTEMPTS)) {
                redisTemplate.expire(key, LOCK_DURATION);
            }
        } catch (Exception e) {
            log.warn("[로그인실패기록실패] loginId={} error={}", loginId, e.getMessage());
        }
    }

    public void recordSuccess(String loginId) {
        try {
            redisTemplate.delete(KEY_PREFIX + loginId);
        } catch (Exception e) {
            log.warn("[로그인성공기록실패] loginId={} error={}", loginId, e.getMessage());
        }
    }

    public boolean isBlocked(String loginId) {
        try {
            String val = redisTemplate.opsForValue().get(KEY_PREFIX + loginId);
            return val != null && Integer.parseInt(val) >= MAX_ATTEMPTS;
        } catch (Exception e) {
            // Redis 장애 시 차단하지 않음 (fail-open)
            log.warn("[로그인차단확인실패] loginId={} error={} - 허용(fail-open)", loginId, e.getMessage());
            return false;
        }
    }

    public int getRemainingAttempts(String loginId) {
        try {
            String val = redisTemplate.opsForValue().get(KEY_PREFIX + loginId);
            if (val == null) return MAX_ATTEMPTS;
            return Math.max(0, MAX_ATTEMPTS - Integer.parseInt(val));
        } catch (Exception e) {
            log.warn("[남은시도횟수확인실패] loginId={} error={}", loginId, e.getMessage());
            return MAX_ATTEMPTS;
        }
    }
}
