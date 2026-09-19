package com.kindtail.adoptmate.animal.dto;

import com.kindtail.adoptmate.animal.domain.Gender;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "보호 동물 등록 요청. 관리자 전용입니다.")
public record AnimalCreateRequest(
        @Schema(description = "동물 종류", example = "DOG", allowableValues = {"DOG", "CAT", "ETC"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "동물 종류를 선택해주세요.")
        Species species,

        @Schema(description = "품종", example = "믹스견", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "품종을 입력해주세요.")
        @Size(max = 100, message = "품종은 100자 이하로 입력해주세요.")
        String breed,

        @Schema(description = "털 색상", example = "갈색", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "털 색상을 입력해주세요.")
        @Size(max = 100, message = "털 색상은 100자 이하로 입력해주세요.")
        String color,

        @Schema(description = "동물 이미지 URL 또는 이미지 데이터", nullable = true, maxLength = 7000000,
                example = "https://example.com/images/animal-1.jpg")
        @Size(max = 7000000, message = "이미지 데이터가 너무 큽니다.")
        String image,

        @Schema(description = "나이. 0 이상이어야 합니다.", example = "3", minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "나이를 입력해주세요.")
        @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
        Long age,

        @Schema(description = "성별", example = "MALE", allowableValues = {"MALE", "FEMALE"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "성별을 선택해주세요.")
        Gender gender,

        @Schema(description = "보호 상태", example = "PROTECTED", allowableValues = {"WAITING", "PROTECTED", "ADOPTED"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "보호 상태를 선택해주세요.")
        Status status
) {
}
