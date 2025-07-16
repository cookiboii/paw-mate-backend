package com.kindtail.adoptmate.common.service;

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
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다!");
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
        // 차단 상태 확인
        if (isBlocked(map.get("email"))) {
            throw new IllegalArgumentException("Blocking");
        }

        // 레디스에 저장된 인증 코드 조회
        String key = VERIFICATION_CODE_KEY + map.get("email");
        Object foundCode = redisTemplate.opsForValue().get(key);
        if (foundCode == null) { // 조회결과가 null? -> 만료됨!
            throw new IllegalArgumentException("authCode expired!");
        }

        // 인증 시도 횟수 증가
        int attemptCount = incrementAttemptCount(map.get("email"));

        // 조회한 코드와 사용자가 입력한 인증번호 검증
        if (!foundCode.toString().equals(map.get("code"))) {
            // 최대 시도 횟수 초과시 차단
            if (attemptCount >= 1000) {
                blockUser(map.get("email"));
                throw new IllegalArgumentException("blocking");
            }
            int remainingAttempts = 1000 - attemptCount;
            throw new IllegalArgumentException(String.format("authCode wrong!, %d", remainingAttempts));
        }


        redisTemplate.delete(key); // 레디스에서 인증번호 삭제
        return map;
    }

    private boolean isBlocked(String email) {
        String key = VERIFICATION_BLOCK_KEY + email;
        return redisTemplate.hasKey(key);
    }

    private void blockUser(String email) {
        String key = VERIFICATION_BLOCK_KEY + email;
        redisTemplate.opsForValue().set(key, "blocked", Duration.ofMinutes(30));
    }

    private int incrementAttemptCount(String email) {
        String key = VERIFICATION_ATTEMPT_KEY + email;
        Object obj = redisTemplate.opsForValue().get(key);

        int count = (obj != null) ? Integer.parseInt(obj.toString()) + 1 : 1;


        redisTemplate.opsForValue().set(key, String.valueOf(count), Duration.ofMinutes(1));

        return count;
    }

    @Transactional
    public void updatePassword(MemberLoginResponseDto updateDto) {
        String email = updateDto.email();
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Email not found")
        );
        String encryptedPassword = passwordEncoder.encode(updateDto.password());
        member.updatePassword(encryptedPassword);
        redisTemplate.delete("reset:" + email);
    }

    private String generateResetCode(){
        return String.valueOf((int) (Math.random() *90000)+10000);
    }


    public  void sendPasswordResetEmail(String email) {


        Optional<Member> byEmail = memberRepository.findByEmail(email);
        if (! byEmail.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다!");
        }

        String authCode ;
        try{
            authCode = generateResetCode();
            mailSenderService.sendAuthCode(email,authCode);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
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
        return true;

    }



}
