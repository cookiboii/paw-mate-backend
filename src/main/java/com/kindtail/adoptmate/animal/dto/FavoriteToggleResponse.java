package com.kindtail.adoptmate.animal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "보호 동물 관심 등록 상태 응답")
public record FavoriteToggleResponse(
        @Schema(description = "동물 ID", example = "1") Long animalId,
        @Schema(description = "현재 로그인 사용자의 관심 등록 여부", example = "true") boolean isFavorite,
        @Schema(description = "변경 후 전체 관심 등록 수", example = "7") long favoriteCount
) {
}
