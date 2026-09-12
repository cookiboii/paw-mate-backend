package com.kindtail.adoptmate.common.service;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.PasswordResetRequest;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MailSenderService mailSenderService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String VERIFICATION_CODE_KEY = "email_verify:code:";
    private static final String VERIFICATION_ATTEMPT_KEY = "email_verify:attempt:";
    private static final String VERIFICATION_BLOCK_KEY = "email_verify:block:";
    private static final String RESET_CODE_KEY = "reset:";
    private static final String RESET_ATTEMPT_KEY = "reset:attempt:";
    private static final String RESET_BLOCK_KEY = "reset:block:";
    private static final String RESET_SEND_KEY = "reset:send:";
    private static final String SIGNUP_SEND_KEY = "signup:send:";
    private static final String SIGNUP_IP_RATE_KEY = "signup:ip:";
    private static final int MAX_SIGNUP_EMAILS_PER_IP = 10;
    private final SecureRandom secureRandom = new SecureRandom();

    public String mailCheck(String email) {
        log.info("Email verification requested for: {}", email);

        Optional<Member> byEmail = memberRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            log.warn("Email already exists: {}", email);
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String authNum;
        try {
            authNum = mailSenderService.joinMail(email);
            log.debug("Verification code generated for {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }

        // 인증 코드를 Redis 에 3분간 저장
        String key = VERIFICATION_CODE_KEY + email;
        redisTemplate.opsForValue().set(key, authNum, Duration.ofMinutes(3));
        log.info("Verification code saved to Redis for {}", email);
        return authNum;
    }

    /** Sends a registration code without revealing whether the account already exists. */
    public void sendRegistrationVerification(String email, String clientIp) {
        enforceSignupIpRateLimit(clientIp);
        Boolean firstRequest = redisTemplate.opsForValue().setIfAbsent(
                SIGNUP_SEND_KEY + email, "sent", Duration.ofMinutes(1)
        );
        if (!Boolean.TRUE.equals(firstRequest)) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }

        try {
            if (memberRepository.findByEmail(email).isPresent()) {
                return;
            }
            String authNum = mailSenderService.joinMail(email);
            redisTemplate.opsForValue().set(VERIFICATION_CODE_KEY + email, authNum, Duration.ofMinutes(3));
        } catch (MessagingException e) {
            redisTemplate.delete(SIGNUP_SEND_KEY + email);
            log.error("Failed to send verification email", e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private void enforceSignupIpRateLimit(String clientIp) {
        String safeClientIp = (clientIp == null || clientIp.isBlank()) ? "unknown" : clientIp;
        String key = SIGNUP_IP_RATE_KEY + safeClientIp;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(10));
        }
        if (count != null && count > MAX_SIGNUP_EMAILS_PER_IP) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }
    }

    // 인증 코드 검증 로직
    public Map<String, String> verifyEmail(Map<String, String> map) {
        String email = map.get("email");
        String code = map.get("code");
        verifyEmail(email, code);
        return map;
    }

    public boolean verifyEmail(String email, String code) {
        log.debug("Verifying email: {}, code: {}", email, code);

        if (email == null || code == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이메일과 인증 코드를 입력해주세요.");
        }

        if (isBlocked(email)) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }

        String key = VERIFICATION_CODE_KEY + email;
        Object foundCode = redisTemplate.opsForValue().get(key);

        if (foundCode == null) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        int attemptCount = incrementAttemptCount(email);
        if (!foundCode.toString().equals(code)) {
            if (attemptCount >= 5) {
                blockUser(email);
                log.warn("User blocked due to 5 consecutive failed email verifications: {}", email);
                throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED, "인증 5회 실패로 30분간 차단됩니다.");
            }
            int remainingAttempts = 5 - attemptCount;
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH,
                    String.format("인증 코드가 일치하지 않습니다. (남은 횟수: %d회)", remainingAttempts));
        }

        log.info("Email verification successful for {}", email);
        redisTemplate.delete(key);
        redisTemplate.delete(VERIFICATION_ATTEMPT_KEY + email);
        // 📌 회원가입 시 검증할 수 있도록 인증 완료 토큰을 Redis에 10분간 보관
        redisTemplate.opsForValue().set("signup_verified:" + email, "true", Duration.ofMinutes(10));
        return true;
    }

    private boolean isBlocked(String email) {
        String key = VERIFICATION_BLOCK_KEY + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private void blockUser(String email) {
        String key = VERIFICATION_BLOCK_KEY + email;
        redisTemplate.opsForValue().set(key, "blocked", Duration.ofMinutes(30));
    }

    private int incrementAttemptCount(String email) {
        String key = VERIFICATION_ATTEMPT_KEY + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(5));
        }
        return count != null ? count.intValue() : 1;
    }

    @Transactional
    public void updatePassword(PasswordResetRequest request) {
        String email = request.email();
        if (email == null || request.password() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이메일과 새 비밀번호를 모두 입력해주세요.");
        }

        Boolean isVerified = redisTemplate.hasKey("reset_verified:" + email);
        if (!Boolean.TRUE.equals(isVerified)) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
        );
        String encryptedPassword = passwordEncoder.encode(request.password());
        member.updatePassword(encryptedPassword);

        redisTemplate.delete("reset:" + email);
        redisTemplate.delete("reset_verified:" + email);
        log.info("Password updated successfully for: {}", email);
    }

    private String generateResetCode() {
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }

    public void sendPasswordResetEmail(String email) {
        Optional<Member> byEmail = memberRepository.findByEmail(email);
        if (byEmail.isEmpty()) {
            // Return normally so callers cannot discover whether this email has an account.
            return;
        }

        if (Boolean.TRUE.equals(redisTemplate.hasKey(RESET_SEND_KEY + email))) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }

        String authCode;
        try {
            authCode = generateResetCode();
            mailSenderService.sendAuthCode(email, authCode);
            log.info("Password reset email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }

        redisTemplate.opsForValue().set(RESET_CODE_KEY + email, authCode, Duration.ofMinutes(5));
        redisTemplate.opsForValue().set(RESET_SEND_KEY + email, "sent", Duration.ofMinutes(1));
    }

    public boolean verifyPassword(String email, String code) {
        if (isResetBlocked(email)) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }

        String key = RESET_CODE_KEY + email;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            return false;
        }
        if (!stored.equals(code)) {
            if (incrementResetAttemptCount(email) >= 5) {
                redisTemplate.delete(key);
                redisTemplate.opsForValue().set(RESET_BLOCK_KEY + email, "blocked", Duration.ofMinutes(30));
                throw new CustomException(ErrorCode.EMAIL_VERIFICATION_BLOCKED);
            }
            return false;
        }
        redisTemplate.delete(key);
        redisTemplate.delete(RESET_ATTEMPT_KEY + email);
        redisTemplate.opsForValue().set("reset_verified:" + email, "true", Duration.ofMinutes(10));
        log.info("Password reset verified successfully for: {}", email);
        return true;
    }

    private boolean isResetBlocked(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RESET_BLOCK_KEY + email));
    }

    private int incrementResetAttemptCount(String email) {
        Long count = redisTemplate.opsForValue().increment(RESET_ATTEMPT_KEY + email);
        if (count != null && count == 1L) {
            redisTemplate.expire(RESET_ATTEMPT_KEY + email, Duration.ofMinutes(5));
        }
        return count != null ? count.intValue() : 1;
    }
}
