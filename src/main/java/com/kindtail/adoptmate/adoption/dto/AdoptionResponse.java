package com.kindtail.adoptmate.adoption.dto;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.domain.HousingType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import com.kindtail.adoptmate.member.domain.Member;

@Schema(description = "입양 신청 응답")
public record AdoptionResponse(
        @Schema(description = "입양 신청 ID", example = "15") Long adoptionId,
        @Schema(description = "입양 대상 동물 ID", example = "3") Long animalId,
        @Schema(description = "입양 대상 동물 품종", example = "믹스견") String animalBreed,
        @Schema(description = "입양 대상 동물 이미지 URL 또는 이미지 데이터", nullable = true,
                example = "https://example.com/images/animal-3.jpg") String animalImage,
        @Schema(description = "신청자 이름. 탈퇴한 회원이면 '탈퇴한 사용자'로 표시됩니다.", example = "홍길동") String userName,
        @Schema(description = "신청자 연락처", example = "010-1234-5678") String phone,
        @Schema(description = "거주 형태", example = "APARTMENT",
                allowableValues = {"APARTMENT", "DETACHED_HOUSE", "VILLA", "ONE_ROOM", "ETC"}) HousingType housingType,
        @Schema(description = "반려동물 유무 및 정보", example = "없음") String hasPet,
        @Schema(description = "입양 동기와 보호 계획", example = "반려동물과 오래 함께할 준비가 되어 신청합니다.") String reason,
        @Schema(description = "신청 처리 상태", example = "PENDING",
                allowableValues = {"PENDING", "APPROVED", "REJECTED"}) AdoptionStatus status,
        @Schema(description = "신청 시각", example = "2026-09-20T14:30:00") LocalDateTime applyDate
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
