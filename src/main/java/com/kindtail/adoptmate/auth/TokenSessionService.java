package com.kindtail.adoptmate.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;

/** Owns Redis-backed refresh-token, blacklist, and token-version state. */
@Service
@RequiredArgsConstructor
public class TokenSessionService {
    private static final DefaultRedisScript<Long> ROTATE_REFRESH_TOKEN_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[3])
                return 1
            end
            return 0
            """, Long.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveRefreshToken(String email, String token, long ttlSeconds) {
        redisTemplate.opsForValue().set("refreshToken:" + email, token, Duration.ofSeconds(ttlSeconds));
    }
    public boolean matchesRefreshToken(String email, String token) {
        Object stored = redisTemplate.opsForValue().get("refreshToken:" + email);
        return stored != null && stored.toString().equals(token);
    }
    public boolean rotateRefreshToken(String email, String currentToken, String nextToken, long ttlSeconds) {
        Long rotated = redisTemplate.execute(
                ROTATE_REFRESH_TOKEN_SCRIPT,
                java.util.List.of("refreshToken:" + email),
                currentToken,
                nextToken,
                ttlSeconds
        );
        return Long.valueOf(1L).equals(rotated);
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
