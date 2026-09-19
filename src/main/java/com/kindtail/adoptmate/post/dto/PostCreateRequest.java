package com.kindtail.adoptmate.post.dto;

import com.kindtail.adoptmate.post.domain.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 작성 요청. 작성자는 요청 본문의 이메일이 아니라 JWT 인증 정보로 결정됩니다.")
public record PostCreateRequest(
        @Schema(description = "게시글 제목", example = "입양 후 한 달 후기", maxLength = 200,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 200, message = "제목은 200자 이하로 작성해주세요.")
        String title,

        @Schema(description = "게시글 본문", example = "새 가족과 건강하게 지내고 있어요.", maxLength = 20000,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(max = 20000, message = "내용은 20,000자 이하로 작성해주세요.")
        String content,

        @Schema(description = "게시글 이미지 URL 또는 이미지 데이터. 이미지가 없으면 생략하거나 null을 전달합니다.",
                example = "https://example.com/images/post-1.jpg", nullable = true, maxLength = 7000000)
        @Size(max = 7000000, message = "이미지 데이터가 너무 큽니다.")
        String img,

        @Schema(description = "게시글 카테고리. 생략하거나 null이면 REVIEW가 적용됩니다.",
                example = "REVIEW", allowableValues = {"REVIEW", "FREE_ADOPTION", "REPORT"},
                defaultValue = "REVIEW", nullable = true)
        PostCategory category
) {
    public PostCreateRequest(String title, String content, String img) {
        this(title, content, img, PostCategory.REVIEW);
    }
}
