package com.kindtail.adoptmate.adoption.repository;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.member.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface AdoptionRepository extends JpaRepository<Adoption, Long> {
     boolean existsByMemberAndAnimal(Member member, Animal animal);
     List<Adoption> findByMember(Member member);


}
