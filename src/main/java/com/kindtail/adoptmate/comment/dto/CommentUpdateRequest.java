package com.kindtail.adoptmate.comment.dto;

import com.kindtail.adoptmate.comment.domain.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 수정 요청. 댓글 작성자 또는 관리자만 수정할 수 있습니다.")
public record CommentUpdateRequest(
        @Schema(description = "댓글 ID. 경로 변수의 commentId를 기준으로 처리하므로 일반 클라이언트 요청에서는 같은 값을 전달합니다.",
                example = "10", nullable = true)
        Long commentId,

        @Schema(description = "수정할 댓글 내용", example = "문의 내용을 수정합니다.", maxLength = 2000,
                requiredMode = Schema.RequiredMode.REQUIRED)
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
