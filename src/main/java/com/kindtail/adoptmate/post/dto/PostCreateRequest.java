package com.kindtail.adoptmate.post.dto;

import com.kindtail.adoptmate.post.domain.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 200, message = "제목은 200자 이하로 작성해주세요.")
        String title,

        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(max = 20000, message = "내용은 20,000자 이하로 작성해주세요.")
        String content,

        @Size(max = 7000000, message = "이미지 데이터가 너무 큽니다.")
        String img,
        PostCategory category
) {
    public PostCreateRequest(String title, String content, String img) {
        this(title, content, img, PostCategory.REVIEW);
    }
}
