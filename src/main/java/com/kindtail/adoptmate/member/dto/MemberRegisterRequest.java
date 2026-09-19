package com.kindtail.adoptmate.member.dto;

import com.kindtail.adoptmate.member.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청. 공개 회원가입 계정은 항상 USER 역할로 생성됩니다.")
public record MemberRegisterRequest(
        @Schema(description = "회원 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        String name,

        @Schema(description = "로그인 및 인증에 사용할 이메일", example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String email,

        @Schema(description = "비밀번호. 최소 6자입니다.", example = "password123", format = "password", minLength = 6,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")
        String password,

        @Schema(description = "호환성을 위해 남아 있는 필드이며 서버에서 무시됩니다. 공개 회원가입은 항상 USER입니다.",
                example = "USER", allowableValues = {"USER"}, defaultValue = "USER", deprecated = true, nullable = true)
        Role role
) {
}
