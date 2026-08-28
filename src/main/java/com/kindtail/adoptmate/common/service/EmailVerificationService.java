package com.kindtail.adoptmate.common.service;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.PasswordResetRequestDto;
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
            log.debug("Verification code generated for {}: {}", email, authNum);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new RuntimeException("이메일 전송 과정 중 문제 발생!");
        }

        // 인증 코드를 Redis 에 3분간 저장
        String key = VERIFICATION_CODE_KEY + email;
        redisTemplate.opsForValue().set(key, authNum, Duration.ofMinutes(3));
        log.info("Verification code saved to Redis for {}", email);
        return authNum;
    }

    // 인증 코드 검증 로직
    public Map<String, String> verifyEmail(Map<String, String> map) {
        String email = map.get("email");
        String code = map.get("code");
        log.debug("Verifying email: {}, code: {}", email, code);

        if (email == null || code == null) {
            throw new IllegalArgumentException("이메일과 인증 코드를 입력해주세요.");
        }

        if (isBlocked(email)) {
            throw new IllegalArgumentException("5회 이상 인증에 실패하여 차단된 상태입니다. 30분 후 다시 시도해주세요.");
        }

        String key = VERIFICATION_CODE_KEY + email;
        Object foundCode = redisTemplate.opsForValue().get(key);

        if (foundCode == null) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다. 다시 전송해주세요.");
        }

        int attemptCount = incrementAttemptCount(email);
        if (!foundCode.toString().equals(code)) {
            if (attemptCount >= 5) {
                blockUser(email);
                log.warn("User blocked due to 5 consecutive failed email verifications: {}", email);
                throw new IllegalArgumentException("인증 5회 실패로 30분간 차단됩니다.");
            }
            int remainingAttempts = 5 - attemptCount;
            throw new IllegalArgumentException(String.format("인증 코드가 일치하지 않습니다. (남은 횟수: %d회)", remainingAttempts));
        }

        log.info("Email verification successful for {}", email);
        redisTemplate.delete(key);
        redisTemplate.delete(VERIFICATION_ATTEMPT_KEY + email);
        return map;
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
    public void updatePassword(PasswordResetRequestDto updateDto) {
        String email = updateDto.email();
        if (email == null || updateDto.password() == null) {
            throw new IllegalArgumentException("이메일과 새 비밀번호를 모두 입력해주세요.");
        }

        Boolean isVerified = redisTemplate.hasKey("reset_verified:" + email);
        if (!Boolean.TRUE.equals(isVerified)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "이메일 인증이 완료되지 않았습니다. 인증을 먼저 진행해주세요.");
        }

        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
        );
        String encryptedPassword = passwordEncoder.encode(updateDto.password());
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
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 회원 이메일입니다!");
        }

        String authCode;
        try {
            authCode = generateResetCode();
            mailSenderService.sendAuthCode(email, authCode);
            log.info("Password reset email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("비밀번호 재설정 이메일 발송 중 오류가 발생했습니다.", e);
        }

        redisTemplate.opsForValue().set("reset:" + email, authCode, Duration.ofMinutes(5));
    }

    public boolean verifyPassword(String email, String code) {
        String key = "reset:" + email;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored == null || !stored.equals(code)) {
            return false;
        }
        redisTemplate.delete(key);
        redisTemplate.opsForValue().set("reset_verified:" + email, "true", Duration.ofMinutes(10));
        log.info("Password reset verified successfully for: {}", email);
        return true;
    }
}
