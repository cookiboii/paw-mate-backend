package com.kindtail.adoptmate.animal.service;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Gender;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.animal.dto.AnimalCreateRequest;
import com.kindtail.adoptmate.animal.dto.AnimalResponse;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.kindtail.adoptmate.member.domain.Role.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AnimalService animalService;

    private Member testMember;
    private Animal testAnimal;
    private SecurityContext securityContext;
    private Authentication authentication;
    private TokenUserInfo tokenUserInfo;

    @BeforeEach
    void setUp() {
        // 테스트용 멤버 생성
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트 사용자")
                .role(ADMIN)
                .build();

        // 테스트용 동물 생성
        testAnimal = Animal.builder()
                .id(1L)
                .species(Species.DOG)
                .breed("진도개")
                .color("황색")
                .gender(Gender.MALE)
                .age(3L)
                .image("http://example.com/image.jpg")
                .status(Status.PROTECTED)
                .member(testMember)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupSecurityContext() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        tokenUserInfo = new TokenUserInfo("test@example.com", ADMIN);

        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(tokenUserInfo);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("동물을 등록할 수 있다")
    void registerAnimal_성공 () {
        // given
        setupSecurityContext();
        AnimalCreateRequest request = new AnimalCreateRequest(
                Species.DOG,
                "진도개",
                "황색",
                "http://example.com/image.jpg",
                3L,
                Gender.MALE,
                Status.PROTECTED
        );

        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(testMember));
        given(animalRepository.save(any(Animal.class))).willReturn(testAnimal);

        // when
        Animal result = animalService.registerAnimal(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSpecies()).isEqualTo(Species.DOG);
        assertThat(result.getBreed()).isEqualTo("진도개");
        verify(memberRepository).findByEmail("test@example.com");
        verify(animalRepository).save(any(Animal.class));
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 예외가 발생한다")
    void registerAnimal_사용자_없음_예외 () {
        // given
        setupSecurityContext();
        AnimalCreateRequest request = new AnimalCreateRequest(
                Species.DOG,
                "진도개",
                "황색",
                "http://example.com/image.jpg",
                3L,
                Gender.MALE,
                Status.PROTECTED
        );

        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> animalService.registerAnimal(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("페이지네이션으로 모든 동물을 조회할 수 있다")
    void getAllAnimals_성공 () {
        // given
        List<Animal> animals = new ArrayList<>();
        animals.add(testAnimal);
        Page<Animal> animalPage = new PageImpl<>(animals, PageRequest.of(0, 10), 1);

        given(animalRepository.findAll(PageRequest.of(0, 10))).willReturn(animalPage);

        // when
        Page<AnimalResponse> result = animalService.getAllAnimals(PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).species()).isEqualTo(Species.DOG);
        verify(animalRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("No-Offset 커서 기반으로 동물 목록을 Slice 조회할 수 있다")
    void getAnimalsByCursor_성공() {
        // given
        org.springframework.data.domain.Slice<Animal> slice = new org.springframework.data.domain.SliceImpl<>(List.of(testAnimal), PageRequest.of(0, 10), false);
        given(animalRepository.findAnimalsByCursor(any(), any(PageRequest.class))).willReturn(slice);

        // when
        org.springframework.data.domain.Slice<AnimalResponse> result = animalService.getAnimalsByCursor(10L, 10);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).species()).isEqualTo(Species.DOG);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("종별로 페이지네이션된 동물 목록을 조회할 수 있다")
    void getAnimalsBySpecies_성공 () {
        // given
        List<Animal> animals = new ArrayList<>();
        animals.add(testAnimal);
        Page<Animal> animalPage = new PageImpl<>(animals, PageRequest.of(0, 10), 1);

        given(animalRepository.findBySpecies(Species.DOG, PageRequest.of(0, 10))).willReturn(animalPage);

        // when
        Page<AnimalResponse> result = animalService.getAnimalsBySpecies(Species.DOG, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).species()).isEqualTo(Species.DOG);
        verify(animalRepository).findBySpecies(Species.DOG, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("ID 로 동물을 조회할 수 있다")
    void getAnimal_성공 () {
        // given
        Long animalId = 1L;
        given(animalRepository.findById(animalId)).willReturn(Optional.of(testAnimal));

        // when
        AnimalResponse result = animalService.getAnimal(animalId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(animalId);
        assertThat(result.species()).isEqualTo(Species.DOG);
        verify(animalRepository).findById(animalId);
    }

    @Test
    @DisplayName("존재하지 않는 동물을 조회하면 예외가 발생한다")
    void getAnimal_없음_예외 () {
        // given
        Long animalId = 999L;
        given(animalRepository.findById(animalId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> animalService.getAnimal(animalId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ANIMAL_NOT_FOUND);
    }

    @Test
    @DisplayName("동물 상태를 업데이트할 수 있다")
    void updateAnimal_성공 () {
        // given
        Long animalId = 1L;
        AnimalStatusUpdateRequest request = new AnimalStatusUpdateRequest(Status.ADOPTED);

        given(animalRepository.findById(animalId)).willReturn(Optional.of(testAnimal));

        // when
        AnimalResponse result = animalService.updateAnimal(animalId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(Status.ADOPTED);
        verify(animalRepository).findById(animalId);
    }

    @Test
    @DisplayName("존재하지 않는 동물의 상태를 업데이트하면 예외가 발생한다")
    void updateAnimal_없음_예외 () {
        // given
        Long animalId = 999L;
        AnimalStatusUpdateRequest request = new AnimalStatusUpdateRequest(Status.ADOPTED);

        given(animalRepository.findById(animalId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> animalService.updateAnimal(animalId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ANIMAL_NOT_FOUND);
    }

    @Test
    @DisplayName("동물을 삭제할 수 있다")
    void deleteAnimal_성공 () {
        // given
        Long animalId = 1L;
        doNothing().when(animalRepository).deleteById(animalId);

        // when
        animalService.deleteAnimal(animalId);

        // then
        verify(animalRepository).deleteById(animalId);
    }
}
