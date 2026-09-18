package com.kindtail.adoptmate.comment.dto;

import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.member.domain.Member;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        String authorName,
        Long authorId,
        String content,
        boolean secret,
        LocalDateTime createdAt,
        List<CommentResponse> children
) {
    public CommentResponse(Long id, String authorName, Long authorId, String content,
                           LocalDateTime createdAt, List<CommentResponse> children) {
        this(id, authorName, authorId, content, false, createdAt, children);
    }

    public static CommentResponse fromComment(Comment comment) {
        return fromComment(comment, null);
    }

    public static CommentResponse fromComment(Comment comment, CustomUserDetails viewer) {
        Member author = comment.getMember();
        return new CommentResponse(
                comment.getId(),
                author != null ? author.getName() : "탈퇴한 사용자",
                author != null ? author.getId() : null,
                canView(comment, viewer) ? comment.getContent() : "비밀 댓글입니다.",
                comment.isSecret(),
                comment.getCreatedAt(),
                comment.getChildren().stream().map(child -> fromComment(child, viewer)).toList()
        );
    }

    private static boolean canView(Comment comment, CustomUserDetails viewer) {
        if (!comment.isSecret()) return true;
        if (viewer == null) return false;
        if (viewer.isAdmin()) return true;
        Long viewerId = viewer.getId();
        if (viewerId == null) return false;
        boolean isCommentAuthor = comment.getMember() != null && viewerId.equals(comment.getMember().getId());
        boolean isPostAuthor = comment.getPost() != null && comment.getPost().getMember() != null
                && viewerId.equals(comment.getPost().getMember().getId());
        return isCommentAuthor || isPostAuthor;
    }
}
