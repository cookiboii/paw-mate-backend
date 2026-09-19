package com.kindtail.adoptmate.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답. Refresh Token 회전 정책에 따라 두 토큰 모두 교체해야 합니다.")
public record TokenRefreshResponse(
        @Schema(description = "새 Access Token", example = "eyJhbGciOiJIUzI1NiJ9.new-access-token") String token,
        @Schema(description = "새 Refresh Token. 기존 Refresh Token 대신 저장해야 합니다.",
                example = "eyJhbGciOiJIUzI1NiJ9.new-refresh-token") String refreshToken
) {
}
