package com.kindtail.adoptmate.adoption.service;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.dto.AdoptionCreateRequest;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.repository.AdoptionRepository;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
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
    public AdoptionResponseDto applyAdoption(AdoptionCreateRequest dto, Long memberId, Long animalId) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANIMAL_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        // 입양 가능한 임시보호(PROTECTED) 상태인지 검증
        if (animal.getStatus() != Status.PROTECTED) {
            throw new CustomException(ErrorCode.NOT_PROTECTED_ANIMAL);
        }
        // 중복 신청 방지
        if (adoptionRepository.existsByMemberAndAnimal(member, animal)) {
            throw new CustomException(ErrorCode.ADOPTION_ALREADY_EXISTS);
        }
        // 📌 3. 세분화된 필드(phone, housingType, hasPet, reason)를 포함하여 엔티티 생성
        Adoption adoption = Adoption.of(
                member,
                animal,
                dto.phone(),
                dto.housingType(),
                dto.hasPet(),
                dto.reason(),
                AdoptionStatus.PENDING
        );
        Adoption saved = adoptionRepository.save(adoption);
        // 📌 4. 신청 접수 시 동물 상태를 '입양 대기(WAITING)'로 자동 전환
        animal.updateStatus(new AnimalStatusUpdateRequest(Status.WAITING));

        return AdoptionResponseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AdoptionResponseDto> getAdoptions(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Adoption> adoptions = adoptionRepository.findByMember(member);
        return adoptions.stream()
                .map(AdoptionResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AdoptionResponseDto> getAllAdoptions(Pageable pageable) {
        Page<Adoption> adoptions = adoptionRepository.findAll(pageable);
        return adoptions.map(AdoptionResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<AdoptionResponseDto> getAllAdoptions() {
        List<Adoption> adoptions = adoptionRepository.findAllWithFetchJoin();
        return adoptions.stream()
                .map(AdoptionResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdoptionResponseDto updateStatus(Long adoptionId, AdoptionStatus status) {
        Adoption adoption = adoptionRepository.findByIdWithFetchJoin(adoptionId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADOPTION_NOT_FOUND));

        // 📌 상태 머신 검증: PENDING 상태일 때만 APPROVED 또는 REJECTED 로 전이 가능
        if (adoption.getStatus() != AdoptionStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_ADOPTION_STATUS_TRANSITION);
        }

        Animal animal = adoption.getAnimal();

        if (status == AdoptionStatus.APPROVED) {
            adoption.updateAdoption(AdoptionStatus.APPROVED);
            animal.updateStatus(new AnimalStatusUpdateRequest(Status.ADOPTED));

            // 📌 연쇄 처리: 한 신청이 최종 승인되면 동일 동물에 대한 다른 대기(PENDING) 신청 건들은 자동 반려
            List<Adoption> otherPendingAdoptions = adoptionRepository.findByAnimalAndStatusAndIdNot(
                    animal, AdoptionStatus.PENDING, adoptionId
            );
            for (Adoption other : otherPendingAdoptions) {
                other.updateAdoption(AdoptionStatus.REJECTED);
            }
        } else if (status == AdoptionStatus.REJECTED) {
            adoption.updateAdoption(AdoptionStatus.REJECTED);

            // 📌 다른 PENDING 신청이 더 이상 없을 때만 동물을 PROTECTED(입양 가능) 상태로 복귀
            boolean hasOtherPending = adoptionRepository.existsByAnimalAndStatusAndIdNot(
                    animal, AdoptionStatus.PENDING, adoptionId
            );
            if (!hasOtherPending) {
                animal.updateStatus(new AnimalStatusUpdateRequest(Status.PROTECTED));
            }
        }

        return AdoptionResponseDto.from(adoption);
    }
}


