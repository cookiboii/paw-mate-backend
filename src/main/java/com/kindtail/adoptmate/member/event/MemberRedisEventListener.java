package com.kindtail.adoptmate.member.event;

import com.kindtail.adoptmate.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;

/** Applies Redis side effects only after the related database transaction commits. */
@Component
@RequiredArgsConstructor
public class MemberRedisEventListener {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void invalidateMemberSession(MemberSessionInvalidationEvent event) {
        redisTemplate.delete("refreshToken:" + event.email());

        String accessToken = event.accessToken();
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }

        long remainingMillis = jwtTokenProvider.getRemainingExpirationMillis(accessToken);
        if (remainingMillis > 0) {
            redisTemplate.opsForValue().set(
                    "blackList:" + accessToken,
                    "logout",
                    Duration.ofMillis(remainingMillis)
            );
        }
    }
}
