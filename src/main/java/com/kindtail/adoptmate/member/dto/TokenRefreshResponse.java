package com.kindtail.adoptmate.member.dto;

public record TokenRefreshResponse(
        String token,
        String refreshToken
) {
}
