package com.kindtail.adoptmate.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberLoginResponseDto(

        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수입니다.")
       String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password



) {
}
