package com.kindtail.adoptmate.animal.dto;

import com.kindtail.adoptmate.animal.domain.Gender;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.domain.Status;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnimalCreateRequest(
        @NotNull(message = "동물 종류를 선택해주세요.")
        Species species,

        @NotBlank(message = "품종을 입력해주세요.")
        String breed,

        @NotBlank(message = "털 색상을 입력해주세요.")
        String color,

        String image,

        @NotNull(message = "나이를 입력해주세요.")
        @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
        Long age,

        @NotNull(message = "성별을 선택해주세요.")
        Gender gender,

        @NotNull(message = "보호 상태를 선택해주세요.")
        Status status
) {
}
