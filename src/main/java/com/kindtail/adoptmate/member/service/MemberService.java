package com.kindtail.adoptmate.member.service;

import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.*;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
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
            TokenUserInfo userInfo = jwtTokenProvider.validateAndTokenUserInfo(accessToken);
            redisTemplate.delete("refreshToken:" + userInfo.getEmail());
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

        return jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
    }

    @Transactional
    public Member registerMember(MemberRegisterRequestDto memberRegisterRequestDto) {
        String email = memberRegisterRequestDto.email();
        String password = memberRegisterRequestDto.password();
        String username = memberRegisterRequestDto.name();

        password = passwordEncoder.encode(password);
        Optional<Member> findMember = memberRepository.findByEmail(email);
        if (findMember.isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Member member = Member.builder()
                .email(email)
                .name(username)
                .password(password)
                .build();
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberLoginResultDto login(MemberLoginResponseDto loginResponseDto) {
        Member member = authenticateMember(loginResponseDto);

        String token = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());
        saveRefreshToken(member.getEmail(), refreshToken);

        return new MemberLoginResultDto(token, refreshToken, member.getEmail(), member.getRole());
    }

    @Transactional(readOnly = true)
    public Member authenticateMember(MemberLoginResponseDto loginResponseDto) {
        String email = loginResponseDto.email();
        String password = loginResponseDto.password();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        return member;
    }

    @Transactional(readOnly = true)
    public Member getMemberInfo() {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return memberRepository.findByEmail(userInfo.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Member> getMembers() {
        return memberRepository.findAll();
    }

    @Transactional
    public void deleteUser(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        memberRepository.delete(member);
    }

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
