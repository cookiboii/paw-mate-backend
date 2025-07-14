package com.kindtail.adoptmate.comment.dto;

import com.kindtail.adoptmate.comment.domain.Comment;

public record CommentUpdateDto(
        Long commentId,
        String content
) {
    public static CommentUpdateDto fromComment(Comment comment) {
        return new CommentUpdateDto(
                comment.getParent() != null ? comment.getParent().getId() :null,
               comment.getContent()

        );
    }
}
