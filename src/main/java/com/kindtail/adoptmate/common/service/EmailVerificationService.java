package com.kindtail.adoptmate.common.service;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

/** Handles registration email verification only. Password reset uses PasswordResetService. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String CODE_KEY = "email_verify:code:";
    private static final String ATTEMPT_KEY = "email_verify:attempt:";
    private static final String BLOCK_KEY = "email_verify:block:";
    private static final String SEND_KEY = "signup:send:";
    private static final String IP_RATE_KEY = "signup:ip:";
    private static final int MAX_EMAILS_PER_IP = 10;

    private final RedisTemplate<String, String> redisTemplate;
    private final MailSenderService mailSenderService;
    private final MemberRepository memberRepository;

    /** Sends a code without revealing whether an account already exists. */
    public void sendRegistrationVerification(String email, String clientIp) {
        enforceIpLimit(clientIp);
        if (!Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(SEND_KEY + email, "sent", Duration.ofMinutes(1)))) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }
        try {
            if (memberRepository.findByEmail(email).isEmpty()) {
                String code = mailSenderService.joinMail(email);
                redisTemplate.opsForValue().set(CODE_KEY + email, code, Duration.ofMinutes(3));
            }
        } catch (MessagingException exception) {
            redisTemplate.delete(SEND_KEY + email);
            log.error("Failed to send registration verification email", exception);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    public Map<String, String> verifyEmail(Map<String, String> request) {
        verifyEmail(request.get("email"), request.get("code"));
        return request;
    }

    public boolean verifyEmail(String email, String code) {
        if (email == null || code == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(BLOCK_KEY + email))) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }
        String savedCode = redisTemplate.opsForValue().get(CODE_KEY + email);
        if (savedCode == null) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }
        if (!savedCode.equals(code)) {
            int attempts = increment(ATTEMPT_KEY + email, Duration.ofMinutes(5));
            if (attempts >= 5) {
                redisTemplate.opsForValue().set(BLOCK_KEY + email, "blocked", Duration.ofMinutes(30));
                throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
            }
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }
        redisTemplate.delete(CODE_KEY + email);
        redisTemplate.delete(ATTEMPT_KEY + email);
        redisTemplate.opsForValue().set("signup_verified:" + email, "true", Duration.ofMinutes(10));
        return true;
    }

    private void enforceIpLimit(String clientIp) {
        String ip = clientIp == null || clientIp.isBlank() ? "unknown" : clientIp;
        if (increment(IP_RATE_KEY + ip, Duration.ofMinutes(10)) > MAX_EMAILS_PER_IP) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }
    }

    private int increment(String key, Duration ttl) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, ttl);
        }
        return count == null ? 1 : count.intValue();
    }
}
