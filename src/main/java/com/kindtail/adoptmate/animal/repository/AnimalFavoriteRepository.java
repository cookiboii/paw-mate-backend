package com.kindtail.adoptmate.animal.repository;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.AnimalFavorite;
import com.kindtail.adoptmate.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnimalFavoriteRepository extends JpaRepository<AnimalFavorite, Long> {

    Optional<AnimalFavorite> findByMemberAndAnimal(Member member, Animal animal);

    boolean existsByMemberIdAndAnimalId(Long memberId, Long animalId);

    long countByAnimalId(Long animalId);

    @Query("SELECT af.animal FROM AnimalFavorite af WHERE af.member.id = :memberId ORDER BY af.createdAt DESC")
    Page<Animal> findFavoriteAnimalsByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    void deleteByMemberAndAnimal(Member member, Animal animal);
}
