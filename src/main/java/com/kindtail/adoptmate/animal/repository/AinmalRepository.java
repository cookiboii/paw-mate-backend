package com.kindtail.adoptmate.animal.repository;

import com.kindtail.adoptmate.animal.domain.Animal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AinmalRepository extends JpaRepository<Animal, Long> {
}
