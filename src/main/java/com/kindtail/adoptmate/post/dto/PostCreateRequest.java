package com.kindtail.adoptmate.post.dto;

import com.kindtail.adoptmate.post.domain.PostCategory;
import jakarta.validation.constraints.NotBlank;

public record PostCreateRequest(
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        String title,

        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        String content,

        String img,
        PostCategory category
) {
    public PostCreateRequest(String title, String content, String img) {
        this(title, content, img, PostCategory.REVIEW);
    }
}
