package com.kindtail.adoptmate.adoption.service;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.domain.HousingType;
import com.kindtail.adoptmate.adoption.dto.AdoptionCreateRequest;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.repository.AdoptionRepository;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AdoptionServiceTest {

    @Mock
    private AdoptionRepository adoptionRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AdoptionService adoptionService;

    private Member member;
    private Animal animal;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .name("홍길동")
                .email("test@example.com")
                .build();

        animal = Animal.builder()
                .id(1L)
                .species(Species.DOG)
                .breed("말티즈")
                .status(Status.PROTECTED)
                .image("test.jpg")
                .build();
    }

    @Test
    @DisplayName("세분화된 필드로 입양 신청을 성공적으로 처리할 수 있다")
    void applyAdoptionSuccess() {
        // given
        AdoptionCreateRequest requestDto = new AdoptionCreateRequest(
                "010-1234-5678",
                HousingType.APARTMENT,
                "없음",
                "평생 책임지고 사랑으로 보살피겠습니다."
        );
        Long memberId = 1L;
        Long animalId = 1L;

        given(animalRepository.findById(animalId)).willReturn(Optional.of(animal));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(adoptionRepository.existsByMemberAndAnimal(member, animal)).willReturn(false);

        Adoption savedAdoption = Adoption.of(
                member,
                animal,
                requestDto.phone(),
                requestDto.housingType(),
                requestDto.hasPet(),
                requestDto.reason(),
                AdoptionStatus.PENDING
        );
        given(adoptionRepository.save(any(Adoption.class))).willReturn(savedAdoption);

        // when
        AdoptionResponseDto response = adoptionService.applyAdoption(requestDto, memberId, animalId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(AdoptionStatus.PENDING);
        assertThat(response.phone()).isEqualTo("010-1234-5678");
        assertThat(response.housingType()).isEqualTo(HousingType.APARTMENT);
        assertThat(response.hasPet()).isEqualTo("없음");
        assertThat(response.reason()).isEqualTo("평생 책임지고 사랑으로 보살피겠습니다.");
        assertThat(animal.getStatus()).isEqualTo(Status.WAITING);
        verify(adoptionRepository).save(any(Adoption.class));
    }

    @Test
    @DisplayName("존재하지 않는 동물로 입양 신청 시 예외가 발생한다")
    void applyAdoptionAnimalNotFound() {
        // given
        AdoptionCreateRequest requestDto = new AdoptionCreateRequest("010-1234-5678", HousingType.APARTMENT, "없음", "동기 작성");
        Long memberId = 1L;
        Long animalId = 999L;

        given(animalRepository.findById(animalId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adoptionService.applyAdoption(requestDto, memberId, animalId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ANIMAL_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 입양 신청 시 예외가 발생한다")
    void applyAdoptionMemberNotFound() {
        // given
        AdoptionCreateRequest requestDto = new AdoptionCreateRequest("010-1234-5678", HousingType.APARTMENT, "없음", "동기 작성");
        Long memberId = 999L;
        Long animalId = 1L;

        given(animalRepository.findById(animalId)).willReturn(Optional.of(animal));
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adoptionService.applyAdoption(requestDto, memberId, animalId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("보호 중이지 않은 동물에 입양 신청 시 예외가 발생한다")
    void applyAdoptionNotProtectedAnimal() {
        // given
        Animal notProtectedAnimal = Animal.builder()
                .id(1L)
                .species(Species.DOG)
                .status(Status.ADOPTED)
                .build();

        AdoptionCreateRequest requestDto = new AdoptionCreateRequest("010-1234-5678", HousingType.APARTMENT, "없음", "동기 작성");
        Long memberId = 1L;
        Long animalId = 1L;

        given(animalRepository.findById(animalId)).willReturn(Optional.of(notProtectedAnimal));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> adoptionService.applyAdoption(requestDto, memberId, animalId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_PROTECTED_ANIMAL);
    }

    @Test
    @DisplayName("이미 입양 신청한 동물에 중복 신청 시 예외가 발생한다")
    void applyAdoptionAlreadyExists() {
        // given
        AdoptionCreateRequest requestDto = new AdoptionCreateRequest("010-1234-5678", HousingType.APARTMENT, "없음", "동기 작성");
        Long memberId = 1L;
        Long animalId = 1L;

        given(animalRepository.findById(animalId)).willReturn(Optional.of(animal));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(adoptionRepository.existsByMemberAndAnimal(member, animal)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adoptionService.applyAdoption(requestDto, memberId, animalId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADOPTION_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("회원의 입양 내역을 조회할 수 있다")
    void getAdoptionsByMember() {
        // given
        Long memberId = 1L;
        Adoption adoption1 = Adoption.of(member, animal, "010-1111-1111", HousingType.APARTMENT, "없음", "이유 1", AdoptionStatus.PENDING);
        Adoption adoption2 = Adoption.of(member, animal, "010-2222-2222", HousingType.VILLA, "개 1마리", "이유 2", AdoptionStatus.APPROVED);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(adoptionRepository.findByMember(member)).willReturn(List.of(adoption1, adoption2));

        // when
        List<AdoptionResponseDto> result = adoptionService.getAdoptions(memberId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).reason()).isEqualTo("이유 1");
        assertThat(result.get(0).housingType()).isEqualTo(HousingType.APARTMENT);
        assertThat(result.get(1).reason()).isEqualTo("이유 2");
        assertThat(result.get(1).housingType()).isEqualTo(HousingType.VILLA);
    }

    @Test
    @DisplayName("전체 입양 내역을 조회할 수 있다")
    void getAllAdoptions() {
        // given
        Adoption adoption1 = Adoption.of(member, animal, "010-1111-1111", HousingType.APARTMENT, "없음", "이유 1", AdoptionStatus.PENDING);
        Adoption adoption2 = Adoption.of(member, animal, "010-2222-2222", HousingType.VILLA, "없음", "이유 2", AdoptionStatus.APPROVED);

        given(adoptionRepository.findAll()).willReturn(List.of(adoption1, adoption2));

        // when
        List<AdoptionResponseDto> result = adoptionService.getAllAdoptions();

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("EntityGraph 기반 페이징으로 전체 입양 내역을 조회할 수 있다")
    void getAllAdoptionsWithPagination() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Adoption adoption1 = Adoption.of(member, animal, "010-1111-1111", HousingType.APARTMENT, "없음", "이유 1", AdoptionStatus.PENDING);
        Page<Adoption> adoptionPage = new PageImpl<>(List.of(adoption1), pageable, 1);

        given(adoptionRepository.findAll(pageable)).willReturn(adoptionPage);

        // when
        Page<AdoptionResponseDto> result = adoptionService.getAllAdoptions(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).userName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("입양 상태를 APPROVED 로 변경하면 동물 상태도 ADOPTED 로 변경된다")
    void updateStatusToApproved() {
        // given
        Long adoptionId = 1L;
        Adoption adoption = Adoption.of(member, animal, "010-1234-5678", HousingType.APARTMENT, "없음", "신청 이유", AdoptionStatus.PENDING);

        given(adoptionRepository.findByIdWithFetchJoin(adoptionId)).willReturn(Optional.of(adoption));

        // when
        AdoptionResponseDto response = adoptionService.updateStatus(adoptionId, AdoptionStatus.APPROVED);

        // then
        assertThat(response.status()).isEqualTo(AdoptionStatus.APPROVED);
        assertThat(animal.getStatus()).isEqualTo(Status.ADOPTED);
    }

    @Test
    @DisplayName("입양 상태를 REJECTED 로 변경하면 동물 상태는 PROTECTED 로 유지된다")
    void updateStatusToRejected() {
        // given
        Long adoptionId = 1L;
        Adoption adoption = Adoption.of(member, animal, "010-1234-5678", HousingType.APARTMENT, "없음", "신청 이유", AdoptionStatus.PENDING);

        given(adoptionRepository.findByIdWithFetchJoin(adoptionId)).willReturn(Optional.of(adoption));

        // when
        AdoptionResponseDto response = adoptionService.updateStatus(adoptionId, AdoptionStatus.REJECTED);

        // then
        assertThat(response.status()).isEqualTo(AdoptionStatus.REJECTED);
        assertThat(animal.getStatus()).isEqualTo(Status.PROTECTED);
    }

    @Test
    @DisplayName("존재하지 않는 입양 신청 ID 로 상태 변경 시 예외가 발생한다")
    void updateStatusNotFound() {
        // given
        Long adoptionId = 999L;
        given(adoptionRepository.findByIdWithFetchJoin(adoptionId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adoptionService.updateStatus(adoptionId, AdoptionStatus.APPROVED))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADOPTION_NOT_FOUND);
    }
}
