package com.kindtail.adoptmate.animal.dto;

import com.kindtail.adoptmate.animal.domain.Gender;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.member.domain.Member;

public record AnimalCreateRequest(
        String species ,
        String breed ,
        String color,
        String image,
        Long age,
        Gender gender,
        Status status,
       Member member


) {
}
