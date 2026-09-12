package com.kindtail.adoptmate.comment.dto;

import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.member.domain.Member;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        String authorName,
        Long authorId,
        String authorEmail,
        String content,
        LocalDateTime createdAt,
        List<CommentResponse> children
) {
    public static CommentResponse fromComment(Comment comment) {
        Member member = comment.getMember();
        return new CommentResponse(
                comment.getId(),
                member != null ? member.getName() : "탈퇴한 사용자",
                member != null ? member.getId() : null,
                member != null ? member.getEmail() : null,
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getChildren().stream()
                        .map(CommentResponse::fromComment)
                        .toList()
        );
    }
}
