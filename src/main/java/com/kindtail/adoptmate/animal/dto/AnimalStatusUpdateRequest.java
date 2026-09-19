package com.kindtail.adoptmate.animal.dto;


import com.kindtail.adoptmate.animal.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "보호 동물 상태 변경 요청. 관리자 전용입니다.")
public record AnimalStatusUpdateRequest(
        @Schema(description = "변경할 보호 상태", example = "ADOPTED",
                allowableValues = {"WAITING", "PROTECTED", "ADOPTED"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "변경할 보호 상태를 선택해주세요.") Status status
){
}
