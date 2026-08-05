package com.kindtail.adoptmate.member.dto;

import com.kindtail.adoptmate.member.domain.Role;

public record MemberLoginResultDto(
        String token,
        String refreshToken,
        String email,
        Role role
) {
}
