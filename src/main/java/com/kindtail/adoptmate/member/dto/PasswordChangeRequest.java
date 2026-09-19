package com.kindtail.adoptmate.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 회원 비밀번호 변경 요청")
public record PasswordChangeRequest(
        @Schema(description = "현재 비밀번호", example = "currentPassword123", format = "password",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @Schema(description = "새 비밀번호. 최소 6자이며 변경 성공 시 기존 토큰이 무효화됩니다.",
                example = "newPassword123", format = "password", minLength = 6,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Size(min = 6, message = "새 비밀번호는 최소 6자 이상이어야 합니다.")
        String newPassword
) {
}
