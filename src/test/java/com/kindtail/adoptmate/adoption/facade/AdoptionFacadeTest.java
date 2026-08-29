package com.kindtail.adoptmate.adoption.facade;

import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.domain.HousingType;
import com.kindtail.adoptmate.adoption.dto.AdoptionCreateRequest;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.repository.AdoptionRepository;
import com.kindtail.adoptmate.adoption.service.AdoptionService;
import com.kindtail.adoptmate.common.lock.DistributedLockTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdoptionFacadeTest {

    @Mock
    private DistributedLockTemplate distributedLockTemplate;

    @Mock
    private AdoptionService adoptionService;

    @Mock
    private AdoptionRepository adoptionRepository;

    @InjectMocks
    private AdoptionFacade adoptionFacade;

    @Test
    @DisplayName("입양 신청 시 animalId 기반으로 분산 락 템플릿을 호출한다")
    @SuppressWarnings("unchecked")
    void applyAdoption_CallsLockTemplate() {
        // given
        Long animalId = 10L;
        Long memberId = 1L;
        AdoptionCreateRequest request = new AdoptionCreateRequest(
                "010-1234-5678", HousingType.APARTMENT, "없음", "이유"
        );
        AdoptionResponseDto expectedResponse = new AdoptionResponseDto(
                1L, animalId, "말티즈", "image.jpg", "신청자",
                "010-1234-5678", HousingType.APARTMENT, "없음", "이유",
                AdoptionStatus.PENDING, LocalDateTime.now()
        );

        given(distributedLockTemplate.execute(eq("animal:10"), any(Supplier.class)))
                .willAnswer(invocation -> {
                    Supplier<AdoptionResponseDto> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        given(adoptionService.applyAdoption(request, memberId, animalId)).willReturn(expectedResponse);

        // when
        AdoptionResponseDto result = adoptionFacade.applyAdoption(request, memberId, animalId);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(distributedLockTemplate).execute(eq("animal:10"), any(Supplier.class));
        verify(adoptionService).applyAdoption(request, memberId, animalId);
    }

    @Test
    @DisplayName("입양 상태 변경 시 animalId 기반으로 분산 락 템플릿을 호출한다")
    @SuppressWarnings("unchecked")
    void updateStatus_CallsLockTemplate() {
        // given
        Long adoptionId = 5L;
        Long animalId = 10L;
        AdoptionResponseDto expectedResponse = new AdoptionResponseDto(
                adoptionId, animalId, "말티즈", "image.jpg", "신청자",
                "010-1234-5678", HousingType.APARTMENT, "없음", "이유",
                AdoptionStatus.APPROVED, LocalDateTime.now()
        );

        com.kindtail.adoptmate.animal.domain.Animal animal = com.kindtail.adoptmate.animal.domain.Animal.builder()
                .id(animalId)
                .build();
        com.kindtail.adoptmate.adoption.domain.Adoption adoption = com.kindtail.adoptmate.adoption.domain.Adoption.builder()
                .id(adoptionId)
                .animal(animal)
                .build();

        given(adoptionRepository.findById(adoptionId)).willReturn(java.util.Optional.of(adoption));
        given(distributedLockTemplate.execute(eq("animal:10"), any(Supplier.class)))
                .willAnswer(invocation -> {
                    Supplier<AdoptionResponseDto> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        given(adoptionService.updateStatus(adoptionId, AdoptionStatus.APPROVED)).willReturn(expectedResponse);

        // when
        AdoptionResponseDto result = adoptionFacade.updateStatus(adoptionId, AdoptionStatus.APPROVED);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(distributedLockTemplate).execute(eq("animal:10"), any(Supplier.class));
        verify(adoptionService).updateStatus(adoptionId, AdoptionStatus.APPROVED);
    }
}
