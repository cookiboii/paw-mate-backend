package com.kindtail.adoptmate.comment.dto;

import com.kindtail.adoptmate.comment.domain.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        Long commentId,

        @NotBlank(message = "수정할 댓글 내용을 입력해주세요.")
        @Size(max = 2000, message = "댓글은 2,000자 이하로 작성해주세요.")
        String content
) {
    public static CommentUpdateRequest fromComment(Comment comment) {
        return new CommentUpdateRequest(
                comment.getId(),
                comment.getContent()
        );
    }
}
