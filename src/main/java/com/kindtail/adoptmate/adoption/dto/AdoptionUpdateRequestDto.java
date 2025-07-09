package com.kindtail.adoptmate.adoption.dto;

import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;

public record AdoptionUpdateRequestDto(
        AdoptionStatus adoptionStatus
) {
}
