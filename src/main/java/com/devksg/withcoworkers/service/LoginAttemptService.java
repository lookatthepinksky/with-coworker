package com.devksg.withcoworkers.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String KEY_PREFIX = "login:fail:";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public void recordFailure(String loginId) {
        String key = KEY_PREFIX + loginId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && (count == 1 || count >= MAX_ATTEMPTS)) {
            redisTemplate.expire(key, LOCK_DURATION);
        }
    }

    public void recordSuccess(String loginId) {
        redisTemplate.delete(KEY_PREFIX + loginId);
    }

    public boolean isBlocked(String loginId) {
        String val = redisTemplate.opsForValue().get(KEY_PREFIX + loginId);
        return val != null && Integer.parseInt(val) >= MAX_ATTEMPTS;
    }

    public int getRemainingAttempts(String loginId) {
        String val = redisTemplate.opsForValue().get(KEY_PREFIX + loginId);
        if (val == null) return MAX_ATTEMPTS;
        return Math.max(0, MAX_ATTEMPTS - Integer.parseInt(val));
    }
}
