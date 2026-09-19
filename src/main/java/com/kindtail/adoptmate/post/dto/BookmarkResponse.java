package com.kindtail.adoptmate.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 북마크 상태 응답")
public record BookmarkResponse(
        @Schema(description = "현재 로그인 사용자의 북마크 여부", example = "true") boolean bookmarked
) { }
