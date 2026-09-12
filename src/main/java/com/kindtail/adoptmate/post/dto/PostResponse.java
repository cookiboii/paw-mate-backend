package com.kindtail.adoptmate.post.dto;

import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.member.domain.Member;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String email,   // ✅ email 먼저
        String name,    // ✅ name 나중
        LocalDateTime createAt,
        String img
) {
    public static PostResponse from(Post post) {
        Member member = post.getMember();
        if (member == null) {
            return new PostResponse(post.getId(), post.getTitle(), post.getContent(), null,
                    "탈퇴한 사용자", post.getCreatedAt(), post.getImage());
        }
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getEmail(), // ✅ 올바른 순서
                member != null ? member.getName() : "탈퇴한 사용자",
                post.getCreatedAt(),
                post.getImage()
        );
    }
}
