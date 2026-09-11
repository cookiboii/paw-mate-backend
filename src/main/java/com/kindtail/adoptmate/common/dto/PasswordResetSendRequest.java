package com.kindtail.adoptmate.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 재설정 코드 발송 요청 DTO")
public record PasswordResetSendRequest(
        @Schema(description = "비밀번호를 재설정할 회원 이메일", example = "user@example.com")
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email
) {}
