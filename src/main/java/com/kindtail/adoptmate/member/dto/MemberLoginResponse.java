package com.kindtail.adoptmate.member.dto;

import com.kindtail.adoptmate.member.domain.Role;

public record MemberLoginResponse(
        String token,
        String refreshToken,
        String email,
        Role role
) {
}
