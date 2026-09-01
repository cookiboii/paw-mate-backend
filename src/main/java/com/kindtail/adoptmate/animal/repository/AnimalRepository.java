package com.kindtail.adoptmate.animal.repository;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Species;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    Page<Animal> findAll(Pageable pageable);

    Page<Animal> findBySpecies(Species species, Pageable pageable);

    Slice<Animal> findSliceBy(Pageable pageable);

    @Query("SELECT a FROM Animal a WHERE (:lastAnimalId IS NULL OR a.id < :lastAnimalId) ORDER BY a.id DESC")
    Slice<Animal> findAnimalsByCursor(@Param("lastAnimalId") Long lastAnimalId, Pageable pageable);

    @Query("SELECT a FROM Animal a WHERE a.species = :species AND (:lastAnimalId IS NULL OR a.id < :lastAnimalId) ORDER BY a.id DESC")
    Slice<Animal> findAnimalsBySpeciesAndCursor(@Param("species") Species species, @Param("lastAnimalId") Long lastAnimalId, Pageable pageable);
}
