package com.kindtail.adoptmate.common.service;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.PasswordResetRequest;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.member.service.MemberService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;

/** Owns password-reset code issuance, verification, and password replacement. */
@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private static final String CODE_KEY = "reset:";
    private static final String ATTEMPT_KEY = "reset:attempt:";
    private static final String BLOCK_KEY = "reset:block:";
    private static final String SEND_KEY = "reset:send:";
    private static final String VERIFIED_KEY = "reset_verified:";

    private final RedisTemplate<String, String> redisTemplate;
    private final MailSenderService mailSenderService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;
    private final SecureRandom secureRandom = new SecureRandom();

    public void sendPasswordResetEmail(String email) {
        if (memberRepository.findByEmail(email).isEmpty()) return;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(SEND_KEY + email))) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }
        try {
            String code = String.valueOf(100000 + secureRandom.nextInt(900000));
            mailSenderService.sendAuthCode(email, code);
            redisTemplate.opsForValue().set(CODE_KEY + email, code, Duration.ofMinutes(5));
            redisTemplate.opsForValue().set(SEND_KEY + email, "sent", Duration.ofMinutes(1));
        } catch (MessagingException exception) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    public boolean verifyPassword(String email, String code) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(BLOCK_KEY + email))) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }
        String savedCode = redisTemplate.opsForValue().get(CODE_KEY + email);
        if (savedCode == null) return false;
        if (!savedCode.equals(code)) {
            if (incrementAttempts(email) >= 5) {
                redisTemplate.delete(CODE_KEY + email);
                redisTemplate.opsForValue().set(BLOCK_KEY + email, "blocked", Duration.ofMinutes(30));
                throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
            }
            return false;
        }
        redisTemplate.delete(CODE_KEY + email);
        redisTemplate.delete(ATTEMPT_KEY + email);
        redisTemplate.opsForValue().set(VERIFIED_KEY + email, "true", Duration.ofMinutes(10));
        return true;
    }

    @Transactional
    public void updatePassword(PasswordResetRequest request) {
        String email = request.email();
        if (email == null || request.password() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(VERIFIED_KEY + email))) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        member.updatePassword(passwordEncoder.encode(request.password()));
        memberService.invalidateSessionsAfterPasswordChange(email);
        redisTemplate.delete(CODE_KEY + email);
        redisTemplate.delete(VERIFIED_KEY + email);
    }

    private int incrementAttempts(String email) {
        Long count = redisTemplate.opsForValue().increment(ATTEMPT_KEY + email);
        if (count != null && count == 1L) redisTemplate.expire(ATTEMPT_KEY + email, Duration.ofMinutes(5));
        return count == null ? 1 : count.intValue();
    }
}
