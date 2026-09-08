package com.kindtail.adoptmate.member.service;

import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.dto.*;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.auth.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    public void logout(String accessToken) {
        try {
            String email = jwtTokenProvider.getEmailFromToken(accessToken);
            redisTemplate.delete("refreshToken:" + email);
        } catch (Exception ignored) {
        }
        long remainingMillis = jwtTokenProvider.getRemainingExpirationMillis(accessToken);
        if (remainingMillis > 0) {
            redisTemplate.opsForValue().set("blackList:" + accessToken, "logout", Duration.ofMillis(remainingMillis));
        }
    }

    public void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(
                "refreshToken:" + email,
                refreshToken,
                Duration.ofSeconds(jwtTokenProvider.getExpirationRt())
        );
    }

    public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "Refresh Token이 제공되지 않았습니다.");
        }
        String email;
        try {
            email = jwtTokenProvider.validateRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "유효하지 않거나 만료된 Refresh Token입니다.");
        }

        Object storedToken = redisTemplate.opsForValue().get("refreshToken:" + email);
        if (storedToken == null || !storedToken.toString().equals(refreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "저장된 Refresh Token 정보와 일치하지 않습니다.");
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return jwtTokenProvider.createToken(member.getId(), member.getEmail(), member.getRole().toString());
    }

    @Value("${app.email-verification.required:false}")
    private boolean emailVerificationRequired;

    @Transactional
    public MemberResponseDto registerMember(MemberRegisterRequestDto memberRegisterRequestDto) {
        String email = memberRegisterRequestDto.email();
        String password = memberRegisterRequestDto.password();
        String username = memberRegisterRequestDto.name();

        // 1. 이메일 중복 검사를 먼저 수행하여 불필요한 BCrypt 연산 비용(CPU 리소스) 낭비 방어
        Optional<Member> findMember = memberRepository.findByEmail(email);
        if (findMember.isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 2. 이메일 인증 완료 상태(signup_verified) 검증 및 1회성 토큰 소비
        Boolean isVerified = redisTemplate.hasKey("signup_verified:" + email);
        if (Boolean.TRUE.equals(isVerified)) {
            redisTemplate.delete("signup_verified:" + email);
        } else if (emailVerificationRequired) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "이메일 인증이 완료되지 않았습니다. 인증을 먼저 진행해주세요.");
        }

        password = passwordEncoder.encode(password);
        Role role = memberRegisterRequestDto.role() != null ? memberRegisterRequestDto.role() : Role.USER;
        Member member = Member.builder()
                .email(email)
                .name(username)
                .password(password)
                .role(role)
                .build();
        Member saved = memberRepository.save(member);
        return MemberResponseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public MemberLoginResultDto login(MemberLoginRequestDto loginRequestDto) {
        Member member = authenticateMember(loginRequestDto);

        String token = jwtTokenProvider.createToken(member.getId(), member.getEmail(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());
        saveRefreshToken(member.getEmail(), refreshToken);

        return new MemberLoginResultDto(token, refreshToken, member.getEmail(), member.getRole());
    }

    @Transactional(readOnly = true)
    public Member authenticateMember(MemberLoginRequestDto loginRequestDto) {
        String email = loginRequestDto.email();
        String password = loginRequestDto.password();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        return member;
    }

    @Transactional(readOnly = true)
    public MemberInfoResponseDto getMemberInfo() {
        String email = SecurityUtil.getCurrentUserEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberInfoResponseDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public List<MemberInfoResponseDto> getMembers() {
        return memberRepository.findAll().stream()
                .map(member -> MemberInfoResponseDto.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .email(member.getEmail())
                        .role(member.getRole())
                        .build())
                .toList();
    }

    @org.springframework.cache.annotation.CacheEvict(value = "userDetails", key = "#email")
    @Transactional
    public void deleteUser(String email) {
        deleteUser(email, null);
    }

   @CacheEvict(value = "userDetails", key = "#email")
    @Transactional
    public void deleteUser(String email, String accessToken) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        memberRepository.delete(member);

        // Redis RefreshToken 삭제
        redisTemplate.delete("refreshToken:" + email);

        // AccessToken Blacklist 등록
        if (accessToken != null && !accessToken.isBlank()) {
            logout(accessToken);
        }
    }

    @CacheEvict(value = "userDetails", key = "#email")
    @Transactional
    public void changePassword(String email, PasswordChangeRequestDto dto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if (!passwordEncoder.matches(dto.currentPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        String encodedNewPassword = passwordEncoder.encode(dto.newPassword());
        member.updatePassword(encodedNewPassword);
    }

    @Transactional(readOnly = true)
    public Long getMemberIdByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return member.getId();
    }
}
