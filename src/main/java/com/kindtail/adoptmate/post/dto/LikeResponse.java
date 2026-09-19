package com.kindtail.adoptmate.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 좋아요 상태 응답")
public record LikeResponse(
        @Schema(description = "현재 로그인 사용자의 좋아요 여부", example = "true") boolean liked,
        @Schema(description = "변경 후 게시글의 전체 좋아요 수", example = "13") long likeCount
) { }
