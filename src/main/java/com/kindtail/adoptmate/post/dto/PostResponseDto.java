package com.kindtail.adoptmate.post.dto;

import com.kindtail.adoptmate.post.domain.Post;

import java.time.LocalDateTime;

public record PostResponseDto(
        Long id,
        String title,
        String content,
        String email,   // ✅ email 먼저
        String name,    // ✅ name 나중
        LocalDateTime createAt,
        String img
) {
    public static PostResponseDto from(Post post) {
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getEmail(), // ✅ 올바른 순서
                post.getMember().getName(),
                post.getCreatedAt(),
                post.getImage()
        );
    }
}
