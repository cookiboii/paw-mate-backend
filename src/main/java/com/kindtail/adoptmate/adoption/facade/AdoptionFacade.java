package com.kindtail.adoptmate.adoption.facade;

import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.dto.AdoptionCreateRequest;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.service.AdoptionService;
import com.kindtail.adoptmate.common.lock.DistributedLockTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdoptionFacade {

    private final DistributedLockTemplate distributedLockTemplate;
    private final AdoptionService adoptionService;

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
     * 입양 신청 ID 기준 분산 락 적용 후 상태 변경
     */
    public AdoptionResponseDto updateStatus(Long adoptionId, AdoptionStatus status) {
        return distributedLockTemplate.execute(
                "adoption:" + adoptionId,
                () -> adoptionService.updateStatus(adoptionId, status)
        );
    }
}
