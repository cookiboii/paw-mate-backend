package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import java.util.Optional;

/**
 * Spring Security Context 및 HTTP 요청에서 인증 정보를 안전하게 처리하는 공통 유틸리티 클래스
 */
public abstract class SecurityUtil {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private SecurityUtil() {
    }

    /**
     * HTTP 요청 헤더에서 Bearer JWT 토큰을 추출
     */
    public static String resolveToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }

    /**
     * 현재 인증된 사용자의 CustomUserDetails 객체 반환
     */
    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            return customUserDetails;
        }

        throw new CustomException(ErrorCode.UNAUTHORIZED, "유효하지 않은 인증 정보입니다.");
    }

    /**
     * 현재 인증된 사용자 정보 반환 (호환성 메서드)
     */
    public static CustomUserDetails getCurrentUserInfo() {
        return getCurrentUserDetails();
    }

    /**
     * 현재 인증된 사용자의 ID(PK) 반환
     */
    public static Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    /** Returns an empty value for anonymous requests (used by public read APIs). */
    public static Optional<Long> getOptionalCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return Optional.ofNullable(userDetails.getId());
        }
        return Optional.empty();
    }

    /**
     * 현재 인증된 사용자의 이메일 반환
     */
    public static String getCurrentUserEmail() {
        return getCurrentUserDetails().getEmail();
    }

    /**
     * 현재 인증된 사용자가 관리자인지 여부 확인
     */
    public static boolean isCurrentUserAdmin() {
        return getCurrentUserDetails().isAdmin();
    }
}
