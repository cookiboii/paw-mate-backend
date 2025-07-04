package com.kindtail.adoptmate.animal.dto;

import com.kindtail.adoptmate.animal.domain.Gender;

public record AnimalCreateRequest(
        String species ,
        String breed ,
        String color,
        String image,
        Long age,
        Gender gender,
        String status



) {
}
