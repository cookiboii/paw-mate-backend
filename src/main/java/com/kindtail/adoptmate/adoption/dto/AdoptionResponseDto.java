package com.kindtail.adoptmate.adoption.dto;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.domain.HousingType;

import java.time.LocalDateTime;

public record AdoptionResponseDto(
        Long adoptionId,
        Long animalId,
        String animalBreed,
        String animalImage,
        String userName,
        String phone,
        HousingType housingType,
        String hasPet,
        String reason,
        AdoptionStatus status,
        LocalDateTime applyDate
) {
    public static AdoptionResponseDto from(Adoption adoption) {
        return new AdoptionResponseDto(
                adoption.getId(),
                adoption.getAnimal().getId(),
                adoption.getAnimal().getBreed(),
                adoption.getAnimal().getImage(),
                adoption.getMember().getName(),
                adoption.getPhone(),
                adoption.getHousingType(),
                adoption.getHasPet(),
                adoption.getReason(),
                adoption.getStatus(),
                adoption.getApplyDate()
        );
    }
}
