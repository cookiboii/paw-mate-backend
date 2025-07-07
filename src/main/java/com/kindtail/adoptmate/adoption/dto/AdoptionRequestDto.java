package com.kindtail.adoptmate.adoption.dto;

import com.kindtail.adoptmate.adoption.domain.Adoption;

public record AdoptionRequestDto(
        Long memberId,
        Long animalId
) {


}
