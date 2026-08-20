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

    @Transactional // 📌 2. 쓰기 작업에만 개별 @Transactional 부여
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
        List<Adoption> adoptions = adoptionRepository.findAll();
        return adoptions.stream()
                .map(AdoptionResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdoptionResponseDto updateStatus(Long adoptionId, AdoptionStatus status) {
        Adoption adoption = adoptionRepository.findByIdWithFetchJoin(adoptionId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADOPTION_NOT_FOUND));
        Animal animal = adoption.getAnimal();
        if (status == AdoptionStatus.REJECTED) {
            adoption.updateAdoption(status);
            animal.updateStatus(new AnimalStatusUpdateRequest(Status.PROTECTED));
        } else if (status == AdoptionStatus.APPROVED) {
            adoption.updateAdoption(status);
            animal.updateStatus(new AnimalStatusUpdateRequest(Status.ADOPTED));
        }
        return AdoptionResponseDto.from(adoption);
    }


}

