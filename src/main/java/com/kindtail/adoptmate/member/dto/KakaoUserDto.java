package com.kindtail.adoptmate.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Properties;

public record KakaoUserDto(
        Long id,

        @JsonProperty("connected_at")
        LocalDateTime connectedAt,

        Properties properties,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    public record Properties(
            String nickname,

            @JsonProperty("profile_image")
            String profileImage,

            @JsonProperty("thumbnail_image")
            String thumbnailImage
    ) {}

    public record KakaoAccount(
            String email
    ) {}
}
