package com.kindtail.adoptmate.member.dto;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberResponseDto(
        @NotBlank(message = "이름은 필수입니다.")
    String Name ,
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email
         ,
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        String password,


        Role role
        ) {
    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
                member.getName(), member.getEmail(), member.getPassword(),  member.getRole()
        );
    }
}
