package com.kindtail.adoptmate.adoption.dto;

import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import jakarta.validation.constraints.NotNull;

public record AdoptionStatusUpdateRequest(
        @NotNull AdoptionStatus adoptionStatus
) {
}
