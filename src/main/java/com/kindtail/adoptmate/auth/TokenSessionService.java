package com.kindtail.adoptmate.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/** Owns Redis-backed refresh-token, blacklist, and token-version state. */
@Service
@RequiredArgsConstructor
public class TokenSessionService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveRefreshToken(String email, String token, long ttlSeconds) {
        redisTemplate.opsForValue().set("refreshToken:" + email, token, Duration.ofSeconds(ttlSeconds));
    }
    public boolean matchesRefreshToken(String email, String token) {
        Object stored = redisTemplate.opsForValue().get("refreshToken:" + email);
        return stored != null && stored.toString().equals(token);
    }
    public void removeRefreshToken(String email) { redisTemplate.delete("refreshToken:" + email); }
    public boolean isBlacklisted(String token) { return Boolean.TRUE.equals(redisTemplate.hasKey("blackList:" + token)); }
    public void blacklist(String token, long ttlMillis) {
        if (ttlMillis > 0) redisTemplate.opsForValue().set("blackList:" + token, "logout", Duration.ofMillis(ttlMillis));
    }
    public long tokenVersion(String email) {
        Object value = redisTemplate.opsForValue().get("tokenVersion:" + email);
        return value instanceof Number number ? number.longValue() : value == null ? 0L : Long.parseLong(value.toString());
    }
    public void invalidateAllTokens(String email) {
        removeRefreshToken(email);
        redisTemplate.opsForValue().increment("tokenVersion:" + email);
    }
}
