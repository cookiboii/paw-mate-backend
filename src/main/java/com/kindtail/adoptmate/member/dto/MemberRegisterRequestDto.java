package com.kindtail.adoptmate.member.dto;

import com.kindtail.adoptmate.member.domain.Role;
import lombok.Builder;
import org.springframework.security.core.userdetails.User;


public record MemberRegisterRequestDto(
        String name,
        String email,
        String password ,
        String address,
        Role role,
       String profile
) {

}
