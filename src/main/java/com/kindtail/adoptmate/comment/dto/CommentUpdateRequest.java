package com.kindtail.adoptmate.comment.dto;

import com.kindtail.adoptmate.comment.domain.Comment;
import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequest(
        Long commentId,

        @NotBlank(message = "수정할 댓글 내용을 입력해주세요.")
        String content
) {
    public static CommentUpdateRequest fromComment(Comment comment) {
        return new CommentUpdateRequest(
                comment.getId(),
                comment.getContent()
        );
    }
}
