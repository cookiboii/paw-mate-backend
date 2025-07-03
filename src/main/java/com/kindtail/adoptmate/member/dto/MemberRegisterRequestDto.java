package com.kindtail.adoptmate.member.dto;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;



public record MemberRegisterRequestDto(
        String name,
        String email,
        String password ,

        Role role

) {

}
