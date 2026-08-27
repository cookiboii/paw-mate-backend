package com.kindtail.adoptmate.member.dto;

import com.kindtail.adoptmate.member.domain.Role;
import lombok.Builder;

@Builder
public record MemberInfoResponseDto(
        Long id,
        String name,
        String email,
        Role role
) {
}
