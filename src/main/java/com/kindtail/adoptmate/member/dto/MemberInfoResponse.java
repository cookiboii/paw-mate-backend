package com.kindtail.adoptmate.member.dto;

import com.kindtail.adoptmate.member.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "현재 로그인 회원 정보 응답")
public record MemberInfoResponse(
        @Schema(description = "회원 ID", example = "1") Long id,
        @Schema(description = "회원 이름", example = "홍길동") String name,
        @Schema(description = "회원 이메일", example = "user@example.com") String email,
        @Schema(description = "회원 역할", example = "USER", allowableValues = {"USER", "ADMIN"}) Role role
) {
}
