package com.kindtail.adoptmate.member.dto;

import com.kindtail.adoptmate.member.domain.AuthProvider;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "회원 응답")
public record MemberResponse(
        @Schema(description = "회원 ID", example = "1") Long id,
        @Schema(description = "회원 이름", example = "홍길동")
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        @Schema(description = "회원 이메일", example = "user@example.com")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,
        @Schema(description = "암호화된 비밀번호 값. 클라이언트에서 사용하지 않습니다.", format = "password",
                accessMode = Schema.AccessMode.READ_ONLY)
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        String password,
        @Schema(description = "회원 역할", example = "USER", allowableValues = {"USER", "ADMIN"}) Role role,
        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profiles/1.jpg", nullable = true) String profileImage,
        @Schema(description = "인증 제공자", example = "EMAIL", allowableValues = {"EMAIL", "KAKAO", "GOOGLE"}) AuthProvider authProvider,
        @Schema(description = "소셜 로그인 제공자의 회원 식별자. 이메일 가입 회원은 null입니다.", nullable = true,
                example = "1234567890") String socialId
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member.getRole(),
                member.getProfileImage(),
                member.getAuthProvider(),
                member.getSocialId()
        );
    }

    public String socialProvider() {
        return authProvider != null ? authProvider.name() : null;
    }
}
