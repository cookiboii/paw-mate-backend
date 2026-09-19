package com.kindtail.adoptmate.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 인증 완료 후 비밀번호 재설정 요청")
public record PasswordResetRequest(

        @Schema(description = "비밀번호를 재설정할 회원 이메일", example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @Schema(description = "새 비밀번호. 최소 6자입니다.", example = "newPassword123", format = "password", minLength = 6,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")
        String password
) {
}
