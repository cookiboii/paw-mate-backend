package com.kindtail.adoptmate.animal.dto;

public record FavoriteToggleResponseDto(
        Long animalId,
        boolean isFavorite,
        long favoriteCount
) {
}
