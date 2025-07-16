package com.kindtail.adoptmate.member.dto;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MemberResponseDto(
        Long id,
        @NotBlank(message = "이름은 필수입니다.")
    String name ,
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email
         ,
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        String password,


        Role role ,
        String profileImage,
        String socialProvider,
        String socialId


        ) {

    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
                member.getId(), member.getName(), member.getEmail(), member.getPassword(), member.getRole(), member.getProfileImage(),
                member.getSocialProvider(),
                member.getSocialId()
                );
    }


}
