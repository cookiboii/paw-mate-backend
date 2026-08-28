package com.kindtail.adoptmate.adoption.repository;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdoptionRepository extends JpaRepository<Adoption, Long> {
     boolean existsByMemberAndAnimal(Member member, Animal animal);

     @Query("select a from Adoption a join fetch a.member join fetch a.animal where a.id = :id")
     Optional<Adoption> findByIdWithFetchJoin(@Param("id") Long id);

     @Query("select a from Adoption a join fetch a.member join fetch a.animal where a.member = :member")
     List<Adoption> findByMember(@Param("member") Member member);

     @Query("select a from Adoption a join fetch a.member join fetch a.animal")
     List<Adoption> findAllWithFetchJoin();

     boolean existsByAnimalIdAndMemberIdAndStatus(Long animalId, Long memberId, AdoptionStatus status);

     List<Adoption> findByAnimalAndStatusAndIdNot(Animal animal, AdoptionStatus status, Long id);

     boolean existsByAnimalAndStatusAndIdNot(Animal animal, AdoptionStatus status, Long id);

     @Override
     @EntityGraph(attributePaths = {"member", "animal"})
     Page<Adoption> findAll(Pageable pageable);
}
