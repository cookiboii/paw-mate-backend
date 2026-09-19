package com.kindtail.adoptmate.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Access Token 재발급 요청")
public record TokenRefreshRequest(
        @Schema(description = "로그인 또는 직전 재발급 시 받은 Refresh Token",
                example = "eyJhbGciOiJIUzI1NiJ9.refresh-token", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Refresh Token은 필수입니다.") String refreshToken
) {
}
