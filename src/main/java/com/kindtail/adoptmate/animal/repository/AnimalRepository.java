package com.kindtail.adoptmate.animal.repository;

import com.kindtail.adoptmate.animal.domain.Animal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
    Optional<Animal> findById(Long Id);

    Animal deleteAnimalById(Long id);
}
