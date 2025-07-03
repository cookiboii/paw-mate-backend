package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.member.domain.Role;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenUserInfo {
    private String email;
    private Role role;
}
