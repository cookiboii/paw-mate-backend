package com.kindtail.adoptmate.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 인증 코드 검증 요청 DTO")
public record EmailVerifyRequest(
        @Schema(description = "인증 대상 이메일", example = "user@example.com")
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "수신한 6자리 인증 코드", example = "123456")
        @NotBlank(message = "인증 코드를 입력해주세요.")
        String code
) {}
