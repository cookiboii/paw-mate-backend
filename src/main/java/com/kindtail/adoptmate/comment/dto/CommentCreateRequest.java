package com.kindtail.adoptmate.comment.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record CommentCreateRequest(
        Long parentId,

        @NotBlank(message = "댓글 내용을 입력해주세요.")
        String content,

        @Schema(description = "비밀 댓글 여부. true이면 댓글 작성자·게시글 작성자·관리자만 내용을 볼 수 있습니다.", example = "false", defaultValue = "false")
        Boolean secret
) {
    public CommentCreateRequest(Long parentId, String content) {
        this(parentId, content, false);
    }
}
