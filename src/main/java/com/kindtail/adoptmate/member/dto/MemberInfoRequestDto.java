package com.kindtail.adoptmate.member.dto;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import lombok.Builder;

@Builder
public record MemberInfoRequestDto(
        Long id,
        String name,
        String email,
        Role role
) {

}
