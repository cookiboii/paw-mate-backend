package com.kindtail.adoptmate.adoption.dto;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.domain.HousingType;

import java.time.LocalDateTime;
import com.kindtail.adoptmate.member.domain.Member;

public record AdoptionResponse(
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
    public static AdoptionResponse from(Adoption adoption) {
        Member member = adoption.getMember();
        return new AdoptionResponse(
                adoption.getId(),
                adoption.getAnimal().getId(),
                adoption.getAnimal().getBreed(),
                adoption.getAnimal().getImage(),
                member != null ? member.getName() : "탈퇴한 사용자",
                adoption.getPhone(),
                adoption.getHousingType(),
                adoption.getHasPet(),
                adoption.getReason(),
                adoption.getStatus(),
                adoption.getApplyDate()
        );
    }
}
