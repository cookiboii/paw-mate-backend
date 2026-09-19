package com.kindtail.adoptmate.member.dto;

import com.kindtail.adoptmate.member.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 성공 응답")
public record MemberLoginResponse(
        @Schema(description = "API 인증에 사용하는 Access Token. Authorization 헤더에 Bearer 형식으로 전달합니다.",
                example = "eyJhbGciOiJIUzI1NiJ9.access-token") String token,
        @Schema(description = "Access Token 재발급에 사용하는 Refresh Token",
                example = "eyJhbGciOiJIUzI1NiJ9.refresh-token") String refreshToken,
        @Schema(description = "로그인 회원 이메일", example = "user@example.com") String email,
        @Schema(description = "로그인 회원 역할", example = "USER", allowableValues = {"USER", "ADMIN"}) Role role
) {
}
