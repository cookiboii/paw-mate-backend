package com.kindtail.adoptmate.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 또는 1단계 대댓글 작성 요청")
public record CommentCreateRequest(
        @Schema(description = "부모 댓글 ID. 최상위 댓글은 생략하거나 null, 대댓글은 최상위 댓글 ID를 전달합니다.",
                example = "10", nullable = true)
        Long parentId,

        @Schema(description = "댓글 내용", example = "입양 관련 문의드립니다.", maxLength = 2000,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 2000, message = "댓글은 2,000자 이하로 작성해주세요.")
        String content,

        @Schema(description = "비밀 댓글 여부. true이면 댓글 작성자·게시글 작성자·관리자만 내용을 볼 수 있습니다. 생략 시 false입니다.",
                example = "false", defaultValue = "false", nullable = true)
        Boolean secret
) {
    public CommentCreateRequest(Long parentId, String content) {
        this(parentId, content, false);
    }
}
