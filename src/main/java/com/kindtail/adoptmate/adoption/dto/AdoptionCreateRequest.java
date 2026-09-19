package com.kindtail.adoptmate.adoption.dto;

import com.kindtail.adoptmate.adoption.domain.HousingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "입양 신청 생성 요청")
public record AdoptionCreateRequest(

        @Schema(description = "신청자 연락처. 하이픈을 포함한 국내 휴대폰 번호 형식입니다.", example = "010-1234-5678",
                pattern = "^01(?:0|1|[6-9])-(?:\\\\d{3}|\\\\d{4})-\\\\d{4}$", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "연락처는 필수 입력 항목입니다.")
        @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
        String phone,
        @Schema(description = "거주 형태", example = "APARTMENT",
                allowableValues = {"APARTMENT", "DETACHED_HOUSE", "VILLA", "ONE_ROOM", "ETC"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "거주 형태를 선택해주세요.")
        HousingType housingType,

        @Schema(description = "현재 반려동물 유무 및 정보", example = "없음", maxLength = 50,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "반려동물 유무를 선택해주세요.")
        @Size(max = 50, message = "반려동물 정보는 50자 이하로 입력해주세요.")
        String hasPet,
        @Schema(description = "입양 동기와 향후 보호 계획", example = "반려동물과 오래 함께할 준비가 되어 신청합니다.",
                minLength = 10, maxLength = 3000, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "입양 동기 및 각오를 작성해주세요.")
        @Size(min = 10, max = 3000, message = "입양 동기는 10자 이상 3,000자 이하로 작성해주세요.")
        String reason
) {
}
