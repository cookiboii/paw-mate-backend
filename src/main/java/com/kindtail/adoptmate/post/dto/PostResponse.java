package com.kindtail.adoptmate.post.dto;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.post.domain.Post;
import java.time.LocalDateTime;

public record PostResponse(
        Long id, String title, String content, String email, String name,
        LocalDateTime createdAt, String img, long likeCount, long commentCount,
        boolean likedByMe, boolean bookmarkedByMe
) {
    public PostResponse(Long id, String title, String content, String email, String name, LocalDateTime createdAt, String img) {
        this(id, title, content, email, name, createdAt, img, 0L, 0L, false, false);
    }

    public static PostResponse from(Post post) {
        Member member = post.getMember();
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(),
                member == null ? null : member.getEmail(), member == null ? "탈퇴한 사용자" : member.getName(),
                post.getCreatedAt(), post.getImage());
    }

    public static PostResponse from(Post post, long likeCount, long commentCount, boolean likedByMe, boolean bookmarkedByMe) {
        PostResponse basic = from(post);
        return new PostResponse(basic.id(), basic.title(), basic.content(), basic.email(), basic.name(), basic.createdAt(),
                basic.img(), likeCount, commentCount, likedByMe, bookmarkedByMe);
    }
}
