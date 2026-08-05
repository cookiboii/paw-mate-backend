package com.kindtail.adoptmate.common.service;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.MemberLoginResponseDto;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
public class EmailVerificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MailSenderService mailSenderService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String VERIFICATION_CODE_KEY = "email_verify:code:";
    private static final String VERIFICATION_ATTEMPT_KEY = "email_verify:attempt:";
    private static final String VERIFICATION_BLOCK_KEY = "email_verify:block:";

    public EmailVerificationService(RedisTemplate<String, String> redisTemplate,
                                    MailSenderService mailSenderService,
                                    MemberRepository memberRepository,
                                    PasswordEncoder passwordEncoder) {
        this.redisTemplate = redisTemplate;
        this.mailSenderService = mailSenderService;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    ;
    }

    public String mailCheck(String email) {
        // 차단 상태 확인


        Optional<Member> byEmail = memberRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String authNum;
        try {
            // 이메일 전송만을 담당하는 객체를 이용해서 이메일 로직 작성.
            authNum = mailSenderService.joinMail(email);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 과정 중 문제 발생!");
        }

        // 인증 코드를 Redis 저장
        String key = VERIFICATION_CODE_KEY + email;
        redisTemplate.opsForValue().set(key, authNum, Duration.ofMinutes(1));

        return authNum;
    }

    // 인증 코드 검증 로직
    public Map<String, String> verifyEmail(Map<String, String> map) {
        String email = map.get("email");
        String code = map.get("code");

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
                throw new IllegalArgumentException("인증 5회 실패로 30분간 차단됩니다.");
            }
            int remainingAttempts = 5 - attemptCount;
            throw new IllegalArgumentException(String.format("인증 코드가 일치하지 않습니다. (남은 횟수: %d회)", remainingAttempts));
        }

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
        Object obj = redisTemplate.opsForValue().get(key);
        int count = (obj != null) ? Integer.parseInt(obj.toString()) + 1 : 1;
        redisTemplate.opsForValue().set(key, String.valueOf(count), Duration.ofMinutes(5));
        return count;
    }

    @Transactional
    public void updatePassword(MemberLoginResponseDto updateDto) {
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
    }

    private String generateResetCode(){
        return String.valueOf((int) (Math.random() * 90000) + 10000);
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
        } catch (MessagingException e) {
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
        return true;
    }



}
