package com.devksg.withcoworkers.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserTeamCacheService {

    private static final String KEY_PREFIX = "userinfo:";

    private final StringRedisTemplate redisTemplate;

    public void saveUserInfo(Long userId, String name, String email, Long teamId, String teamName, boolean isAdmin) {
        log.info("[유저캐시저장시도] userId={} name={} email={} teamId={} teamName={} isAdmin={}", userId, name, email, teamId, teamName, isAdmin);
        try {
            var ops = redisTemplate.<String, String>opsForHash();
            String key = KEY_PREFIX + userId;
            ops.put(key, "name", name != null ? name : "");
            ops.put(key, "email", email != null ? email : "");
            if (teamId != null) {
                ops.put(key, "teamId", String.valueOf(teamId));
                ops.put(key, "teamName", teamName != null ? teamName : "");
                ops.put(key, "isAdmin", String.valueOf(isAdmin));
            }
        } catch (Exception e) {
            log.warn("[유저캐시저장실패] userId={}", userId, e);
        }
    }

    public Optional<Map<Object, Object>> getUserInfo(Long userId) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(KEY_PREFIX + userId);
            if (entries.isEmpty()) return Optional.empty();
            return Optional.of(entries);
        } catch (Exception e) {
            log.warn("[유저캐시조회실패] userId={} error={}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    public void deleteUserInfo(Long userId) {
        try {
            redisTemplate.delete(KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("[유저캐시삭제실패] userId={}", userId, e);
        }
    }

    public Optional<Long> getTeamId(Long userId) {
        try {
            Object value = redisTemplate.opsForHash().get(KEY_PREFIX + userId, "teamId");
            if (value == null) return Optional.empty();
            return Optional.of(Long.parseLong((String) value));
        } catch (Exception e) {
            log.warn("[팀캐시조회실패] userId={} error={}", userId, e.getMessage());
            return Optional.empty();
        }
    }
}
