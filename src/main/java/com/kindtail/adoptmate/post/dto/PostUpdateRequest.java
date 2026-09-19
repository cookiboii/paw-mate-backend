package com.kindtail.adoptmate.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 수정 요청. 작성자 본인만 요청할 수 있으며 title과 content를 모두 보내야 합니다.")
public record PostUpdateRequest(
        @Schema(description = "수정할 게시글 제목", example = "입양 후 두 달 후기", maxLength = 200,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 200, message = "제목은 200자 이하로 작성해주세요.")
        String title,

        @Schema(description = "수정할 이미지 URL 또는 이미지 데이터. null이면 기존 이미지가 제거됩니다.",
                example = "https://example.com/images/post-1-updated.jpg", nullable = true, maxLength = 7000000)
        @Size(max = 7000000, message = "이미지 데이터가 너무 큽니다.")
        String img,

        @Schema(description = "수정할 게시글 본문", example = "두 달째 건강하게 지내고 있어요.", maxLength = 20000,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(max = 20000, message = "내용은 20,000자 이하로 작성해주세요.")
        String content
) {
}
