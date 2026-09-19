package com.kindtail.adoptmate.adoption.dto;

import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "입양 신청 상태 변경 요청. 관리자 전용입니다.")
public record AdoptionStatusUpdateRequest(
        @Schema(description = "변경할 신청 상태", example = "APPROVED",
                allowableValues = {"PENDING", "APPROVED", "REJECTED"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "변경할 입양 신청 상태를 선택해주세요.") AdoptionStatus adoptionStatus
) {
}
