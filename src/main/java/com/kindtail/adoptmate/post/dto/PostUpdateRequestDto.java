package com.kindtail.adoptmate.post.dto;

import jakarta.validation.constraints.NotBlank;

public record PostUpdateRequestDto(
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        String title,

        String img,

        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        String content
) {
}
