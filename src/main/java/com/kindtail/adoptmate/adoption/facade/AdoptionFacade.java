package com.kindtail.adoptmate.adoption.facade;

import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.dto.AdoptionCreateRequest;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.repository.AdoptionRepository;
import com.kindtail.adoptmate.adoption.service.AdoptionService;
import com.kindtail.adoptmate.common.lock.DistributedLockTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdoptionFacade {

    private final DistributedLockTemplate distributedLockTemplate;
    private final AdoptionService adoptionService;
    private final AdoptionRepository adoptionRepository;

    /**
     * 동물 ID 기준 분산 락 적용 후 입양 신청
     */
    public AdoptionResponseDto applyAdoption(AdoptionCreateRequest dto, Long memberId, Long animalId) {
        return distributedLockTemplate.execute(
                "animal:" + animalId,
                () -> adoptionService.applyAdoption(dto, memberId, animalId)
        );
    }

    /**
     * 동물 ID 기준 분산 락 적용 후 입양 상태 변경 (동물 상태 변경 및 연쇄 반려의 데이터 무결성 보장)
     */
    public AdoptionResponseDto updateStatus(Long adoptionId, AdoptionStatus status) {
        Long animalId = adoptionRepository.findById(adoptionId)
                .map(a -> a.getAnimal() != null ? a.getAnimal().getId() : null)
                .orElse(null);

        String lockKey = animalId != null ? "animal:" + animalId : "adoption:" + adoptionId;

        return distributedLockTemplate.execute(
                lockKey,
                () -> adoptionService.updateStatus(adoptionId, status)
        );
    }
}
