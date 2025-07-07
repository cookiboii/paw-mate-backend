package com.kindtail.adoptmate.adoption.dto;

import com.kindtail.adoptmate.adoption.domain.Adoption;

public record AdoptionResponseDto(
        Long adoptionId,
        String memberName,   // ✅ 신청자 이름
        String status,
        String applyDate
) {
    public static AdoptionResponseDto from(Adoption adoption) {
        return new AdoptionResponseDto(
                adoption.getId(),
                adoption.getMember().getName(),           // ✅ 신청자 이름
                adoption.getStatus().name(),
                adoption.getApply_date().toString()
        );
    }
}
