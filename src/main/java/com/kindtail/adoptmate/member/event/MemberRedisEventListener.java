package com.kindtail.adoptmate.member.event;

import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.auth.TokenSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


/** Applies Redis side effects only after the related database transaction commits. */
@Component
@RequiredArgsConstructor
public class MemberRedisEventListener {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenSessionService tokenSessionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void invalidateMemberSession(MemberSessionInvalidationEvent event) {
        tokenSessionService.invalidateAllTokens(event.email());

        String accessToken = event.accessToken();
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }

        long remainingMillis = jwtTokenProvider.getRemainingExpirationMillis(accessToken);
        tokenSessionService.blacklist(accessToken, remainingMillis);
    }
}
