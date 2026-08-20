package com.kindtail.adoptmate.adoption.dto;

import com.kindtail.adoptmate.adoption.domain.HousingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdoptionCreateRequest(

        @NotBlank(message = "연락처는 필수 입력 항목입니다.")
        @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
        String phone,
        // 📌 Enum 적용 (NotNull 검증)
        @NotNull(message = "거주 형태를 선택해주세요.")
        HousingType housingType,
        @NotBlank(message = "반려동물 유무를 선택해주세요.")
        String hasPet,
        @NotBlank(message = "입양 동기 및 각오를 작성해주세요.")
        @Size(min = 10, message = "입양 동기는 10자 이상 작성해주세요.")
        String reason
) {
}
