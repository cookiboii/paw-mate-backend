package com.kindtail.adoptmate.animal.dto;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Gender;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "보호 동물 응답")
public record AnimalResponse(
        @Schema(description = "동물 ID", example = "1") Long id,
        @Schema(description = "동물 종류", example = "DOG", allowableValues = {"DOG", "CAT", "ETC"}) Species species,
        @Schema(description = "품종", example = "믹스견") String breed,
        @Schema(description = "털 색상", example = "갈색") String color,
        @Schema(description = "보호 상태", example = "PROTECTED",
                allowableValues = {"WAITING", "PROTECTED", "ADOPTED"}) Status status,
        @Schema(description = "나이", example = "3", minimum = "0") Long age,
        @Schema(description = "성별", example = "MALE", allowableValues = {"MALE", "FEMALE"}) Gender gender,
        @Schema(description = "동물 이미지 URL 또는 이미지 데이터", nullable = true,
                example = "https://example.com/images/animal-1.jpg") String image
) {

    public static AnimalResponse from(Animal animal) {
        return new AnimalResponse(
                animal.getId(),
                animal.getSpecies(),
                animal.getBreed(),
                animal.getColor(),
                animal.getStatus(),
                animal.getAge(),
                animal.getGender(),
                animal.getImage()
        );
    }
}
