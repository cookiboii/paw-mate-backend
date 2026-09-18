package com.kindtail.adoptmate.member.service;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.dto.*;
import com.kindtail.adoptmate.member.event.MemberSessionInvalidationEvent;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.auth.CurrentUserProvider;
import com.kindtail.adoptmate.auth.TokenSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUserProvider currentUserProvider;
    private final TokenSessionService tokenSessionService;

    @Value("${app.email-verification.required:false}")
    private boolean emailVerificationRequired;

    @Transactional
    public MemberResponse registerMember(MemberRegisterRequest request) {
        String email = request.email();
        String password = request.password();
        String username = request.name();

        // 1. 이메일 중복 검사를 먼저 수행하여 불필요한 BCrypt 연산 비용(CPU 리소스) 낭비 방어
        Optional<Member> findMember = memberRepository.findByEmail(email);
        if (findMember.isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 이메일 인증 표시는 회원 저장 트랜잭션이 커밋된 뒤에만 소비한다.
        String verificationKey = "signup_verified:" + email;
        Boolean isVerified = redisTemplate.hasKey(verificationKey);
        if (!Boolean.TRUE.equals(isVerified) && emailVerificationRequired) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "이메일 인증이 완료되지 않았습니다. 인증을 먼저 진행해주세요.");
        }

        password = passwordEncoder.encode(password);
        // Public registration must never be able to select a privileged role.
        Role role = Role.USER;
        Member member = Member.builder()
                .email(email)
                .name(username)
                .password(password)
                .role(role)
                .build();
        Member saved = memberRepository.save(member);
        if (Boolean.TRUE.equals(isVerified)) {
            deleteRedisKeyAfterCommit(verificationKey);
        }
        return MemberResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse getMemberInfo() {
        String email = currentUserProvider.currentUserEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberInfoResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public List<MemberInfoResponse> getMembers() {
        return memberRepository.findAll().stream()
                .map(member -> MemberInfoResponse.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .email(member.getEmail())
                        .role(member.getRole())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MemberInfoResponse> getMembers(Pageable pageable) {
        return memberRepository.findAll(pageable).map(member -> MemberInfoResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .build());
    }

    @Transactional
    public void deleteUser(String email) {
        deleteUser(email, null);
    }

    @Transactional
    public void deleteUser(String email, String accessToken) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        memberRepository.delete(member);
        eventPublisher.publishEvent(new MemberSessionInvalidationEvent(email, accessToken));

    }

    /**
     * 관리자에 의한 회원 강제 삭제 (Soft Delete 및 토큰/캐시 무효화)
     */
    @Transactional
    public void deleteMemberByAdmin(Long memberId, Long adminId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (adminId != null && member.getId().equals(adminId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "관리자 본인 계정은 관리자 회원 삭제 기능으로 삭제할 수 없습니다.");
        }

        if (member.getRole() == Role.ADMIN) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_AUTHOR, "관리자 계정은 삭제할 수 없습니다.");
        }

        String email = member.getEmail();
        memberRepository.delete(member);
        eventPublisher.publishEvent(new MemberSessionInvalidationEvent(email, null));
    }

    @Transactional
    public void deleteMemberByAdmin(Long memberId) {
        deleteMemberByAdmin(memberId, currentUserProvider.currentUserId());
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest dto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if (!passwordEncoder.matches(dto.currentPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        String encodedNewPassword = passwordEncoder.encode(dto.newPassword());
        member.updatePassword(encodedNewPassword);
        invalidateSessionsAfterPasswordChange(email);
    }

    /** Invalidates every access/refresh token issued before a password change. */
    public void invalidateSessionsAfterPasswordChange(String email) {
        Runnable invalidate = () -> tokenSessionService.invalidateAllTokens(email);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    invalidate.run();
                }
            });
        } else {
            invalidate.run();
        }
    }

    private void deleteRedisKeyAfterCommit(String key) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            redisTemplate.delete(key);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisTemplate.delete(key);
            }
        });
    }

    @Transactional(readOnly = true)
    public Long getMemberIdByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return member.getId();
    }
}
