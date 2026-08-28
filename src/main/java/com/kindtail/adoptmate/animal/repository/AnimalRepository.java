package com.kindtail.adoptmate.animal.repository;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Species;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
    Optional<Animal> findById(Long Id);

    @Modifying(clearAutomatically = true)
    @Query("update Animal a set a.isDeleted = true where a.id = :id")
    void deleteAnimalById(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"member"})
    Page<Animal> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    Page<Animal> findBySpecies(Species species, Pageable pageable);
}
