package com.kindtail.adoptmate.adoption.dto;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;

public record AdoptionRequestDto(
        Long memberId,
        Long animalId ,
        String interview,
        AdoptionStatus status
) {


}
