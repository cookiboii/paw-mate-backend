package com.kindtail.adoptmate.animal.service;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Gender;
import com.kindtail.adoptmate.animal.dto.AnimalCreateRequest;
import com.kindtail.adoptmate.animal.dto.AnimalResponse;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnimalService {

   private final AnimalRepository animalRepository;

    public AnimalService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }
   @Transactional
    public Animal registerAnimal (AnimalCreateRequest animalCreateRequest){
     String species =  animalCreateRequest.species();
     String  breed   =  animalCreateRequest.breed();
     String  color   =  animalCreateRequest.color();
      String    status  = animalCreateRequest.status();
       Gender gender = animalCreateRequest.gender();
       Long   age = animalCreateRequest.age();
       Animal animal = Animal.builder().species(species).age(age).breed(breed).color(color).status(status).gender(gender).build();


       return animalRepository.save(animal);
    }
   @Transactional(readOnly = true)
   public Page <AnimalResponse> getAllAnimals(Pageable pageable){
               Page<Animal> animals = animalRepository.findAll(pageable);//
             return animals.map(AnimalResponse::from);

    }
}
