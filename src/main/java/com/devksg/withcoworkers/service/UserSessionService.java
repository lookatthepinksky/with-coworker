package com.devksg.withcoworkers.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSessionService {

    private static final String KEY_PREFIX = "session:";

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public void save(Long userId, String token) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + userId, token, Duration.ofMillis(expirationMs));
        } catch (Exception e) {
            log.warn("[세션저장실패] userId={} error={}", userId, e.getMessage());
        }
    }

    public boolean isValid(Long userId, String token) {
        try {
            String stored = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
            return token.equals(stored);
        } catch (Exception e) {
            // Redis 장애 시 기존 JWT 검증만으로 허용 (fail-open)
            log.warn("[세션확인실패] userId={} error={} - 허용(fail-open)", userId, e.getMessage());
            return true;
        }
    }

    public void invalidate(Long userId) {
        try {
            redisTemplate.delete(KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("[세션삭제실패] userId={} error={}", userId, e.getMessage());
        }
    }
}
