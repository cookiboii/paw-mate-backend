package com.kindtail.adoptmate.animal.dto;

public record FavoriteToggleResponse(
        Long animalId,
        boolean isFavorite,
        long favoriteCount
) {
}
