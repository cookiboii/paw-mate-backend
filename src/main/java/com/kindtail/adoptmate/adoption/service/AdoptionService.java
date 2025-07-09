package com.kindtail.adoptmate.adoption.service;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.dto.AdoptionRequestDto;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.repository.AdoptionRepository;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Adoption applyAdoption(AdoptionRequestDto dto,Long memberId ,Long animalId) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new IllegalArgumentException("Animal not found"));
       Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Member not found"));
        if (animal.getStatus() != Status.PROTECTED) {
            throw new IllegalStateException("보호 중인 동물만 입양할 수 있습니다.");
        }

        if (adoptionRepository.existsByMemberAndAnimal(member, animal)) {
            throw new IllegalStateException("이미 신청한 동물입니다.");
        }

        Adoption adoption = Adoption.of(member, animal, dto.interview(), AdoptionStatus.PENDING );
        return adoptionRepository.save(adoption);
    }
     @Transactional(readOnly = true)
      public List<AdoptionResponseDto> getAdoptions(Long  memberId) {
        Member member  = memberRepository.findById( memberId).orElseThrow(() -> new IllegalArgumentException("Member not found"));

    List<Adoption> adoptions = adoptionRepository.findByMember(member);

        return adoptions.stream().map(
               AdoptionResponseDto::from
        ).collect(Collectors.toList());

      }


      @Transactional(readOnly = true)
       public List<AdoptionResponseDto> getAllAdoptions() {

         List<Adoption> adoptions = adoptionRepository.findAll();
         return adoptions.stream()
                 .map( AdoptionResponseDto::from )
                 .collect(Collectors.toList());
       }
       @Transactional
       public Adoption updateStatus(Long adoptionId, AdoptionStatus status) {
          Adoption adoption = adoptionRepository.findById( adoptionId).orElseThrow(() -> new IllegalArgumentException("Adoption not found"));
          if (status == AdoptionStatus.REJECTED) {
             adoptionRepository.delete(adoption);


          }
          else if (status == AdoptionStatus.APPROVED) {
             adoption.updateAdoption(status);
             Animal animal =adoption.getAnimal();
             animal.updatestatus(new AnimalStatusUpdateRequest(Status.ADOPTED));
          }
          return adoption;
       }


}

