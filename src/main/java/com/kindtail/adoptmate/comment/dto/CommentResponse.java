package com.kindtail.adoptmate.comment.dto;

import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "댓글 응답. 비밀 댓글을 볼 권한이 없으면 content가 '비밀 댓글입니다.'로 마스킹됩니다.")
public record CommentResponse(
        @Schema(description = "댓글 ID", example = "10") Long id,
        @Schema(description = "작성자 표시 이름. 탈퇴한 회원이면 '탈퇴한 사용자'로 표시됩니다.", example = "홍길동") String authorName,
        @Schema(description = "작성자 회원 ID. 탈퇴한 회원이면 null입니다.", example = "1", nullable = true) Long authorId,
        @Schema(description = "댓글 내용 또는 권한이 없는 비밀 댓글의 마스킹 문구", example = "입양 관련 문의드립니다.") String content,
        @Schema(description = "비밀 댓글 여부", example = "false") boolean secret,
        @Schema(description = "댓글 작성 시각", example = "2026-09-20T14:30:00") LocalDateTime createdAt,
        @Schema(description = "대댓글 목록. 최상위 댓글 조회 시 1단계 대댓글이 포함됩니다.") List<CommentResponse> children
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
