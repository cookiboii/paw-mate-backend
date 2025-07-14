package com.kindtail.adoptmate.animal.dto;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Gender;
import com.kindtail.adoptmate.animal.domain.Status;

public record AnimalResponse(

        Long id,
        String species,
        String breed,
        String color,
        Status status,
        Long age,
        Gender gender ,
        String image




) {

    public static AnimalResponse from(Animal animal) {
        return new AnimalResponse(
                animal.getId(),
                animal.getSpecies(),
                animal.getBreed(),
                animal.getColor(),
                animal.getStatus(),
                animal.getAge(),
                animal.getGender(),
                animal.getImage()
        );
    }
}
