package com.kindtail.adoptmate.adoption.service;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.dto.AdoptionRequestDto;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.repository.AdoptionRepository;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AdoptionService {

   private final AdoptionRepository adoptionRepository;
  private final AnimalRepository animalRepository;
  private final MemberRepository memberRepository;
    public AdoptionService(AdoptionRepository adoptionRepository, AnimalRepository animalRepository, MemberRepository memberRepository) {
        this.adoptionRepository = adoptionRepository;
        this.animalRepository = animalRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Adoption applyAdoption(AdoptionRequestDto  adoptionRequestDto) {

        Animal animal = animalRepository.findById(adoptionRequestDto.animalId()).orElseThrow(
                () -> new IllegalArgumentException("Animal not found")
        );
       Member member = memberRepository.findById(adoptionRequestDto.memberId()).orElseThrow(
               () -> new IllegalArgumentException("Member not found")
       );
     if ( animal.getStatus() != Status.PROTECTED){
         throw new IllegalStateException("보호 중인 동물만 입양할 수 있습니다.");
     }
       if ( adoptionRepository.existsByMemberAndAnimal(member, animal) ){
           throw new IllegalStateException("이미 신청한 동물입니다.");
       }
       Adoption adoption =Adoption.of(member, animal);
       return adoptionRepository.save(adoption);
    }




}

