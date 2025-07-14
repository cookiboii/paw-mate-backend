package com.kindtail.adoptmate.adoption.dto;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;

public record AdoptionResponseDto(
        Long adoptionId,
        String memberName,   // ✅ 신청자 이름
        AdoptionStatus  status,
        String interviewer,
         String animalImage,
        String applyDate
) {
    public static AdoptionResponseDto from(Adoption adoption) {
        return new AdoptionResponseDto(
                adoption.getId(),
                adoption.getMember().getName(),
                adoption.getStatus(),
                adoption.getInterview(),
                adoption.getAnimal().getImage(),
                adoption.getApply_date().toString()
        );
    }
}
