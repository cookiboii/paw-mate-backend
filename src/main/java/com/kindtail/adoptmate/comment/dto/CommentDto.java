package com.kindtail.adoptmate.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentDto(
        Long parentId,

        @NotBlank(message = "댓글 내용을 입력해주세요.")
        String content
) {
}
