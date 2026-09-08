package com.kindtail.adoptmate.animal.service;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.AnimalFavorite;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.animal.dto.AnimalResponse;
import com.kindtail.adoptmate.animal.dto.FavoriteToggleResponseDto;
import com.kindtail.adoptmate.animal.repository.AnimalFavoriteRepository;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalFavoriteServiceTest {

    @Mock
    private AnimalFavoriteRepository animalFavoriteRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AnimalFavoriteService animalFavoriteService;

    private Member testMember;
    private Animal testAnimal;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .email("user@example.com")
                .name("사용자")
                .role(Role.USER)
                .build();

        testAnimal = Animal.builder()
                .id(1L)
                .species(Species.DOG)
                .breed("시바견")
                .color("황색")
                .status(Status.PROTECTED)
                .build();
    }

    @Test
    @DisplayName("처음 찜하기를 누르면 찜 목록에 등록된다 (isFavorite = true)")
    void toggleFavorite_등록_성공() {
        // given
        Long animalId = 1L;
        Long memberId = 1L;

        given(animalRepository.findById(animalId)).willReturn(Optional.of(testAnimal));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(testMember));
        given(animalFavoriteRepository.findByMemberAndAnimal(testMember, testAnimal)).willReturn(Optional.empty());
        given(animalFavoriteRepository.countByAnimalId(animalId)).willReturn(1L);

        // when
        FavoriteToggleResponseDto response = animalFavoriteService.toggleFavorite(animalId, memberId);

        // then
        assertThat(response.animalId()).isEqualTo(animalId);
        assertThat(response.isFavorite()).isTrue();
        assertThat(response.favoriteCount()).isEqualTo(1L);
        verify(animalFavoriteRepository).save(any(AnimalFavorite.class));
        verify(animalFavoriteRepository, never()).delete(any());
    }

    @Test
    @DisplayName("이미 찜한 상태에서 다시 누르면 찜이 취소된다 (isFavorite = false)")
    void toggleFavorite_취소_성공() {
        // given
        Long animalId = 1L;
        Long memberId = 1L;
        AnimalFavorite existingFavorite = AnimalFavorite.builder()
                .member(testMember)
                .animal(testAnimal)
                .build();

        given(animalRepository.findById(animalId)).willReturn(Optional.of(testAnimal));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(testMember));
        given(animalFavoriteRepository.findByMemberAndAnimal(testMember, testAnimal)).willReturn(Optional.of(existingFavorite));
        given(animalFavoriteRepository.countByAnimalId(animalId)).willReturn(0L);

        // when
        FavoriteToggleResponseDto response = animalFavoriteService.toggleFavorite(animalId, memberId);

        // then
        assertThat(response.animalId()).isEqualTo(animalId);
        assertThat(response.isFavorite()).isFalse();
        assertThat(response.favoriteCount()).isEqualTo(0L);
        verify(animalFavoriteRepository).delete(existingFavorite);
        verify(animalFavoriteRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 동물을 찜하려고 하면 예외가 발생한다")
    void toggleFavorite_동물_없음_예외() {
        // given
        Long nonExistentAnimalId = 999L;
        Long memberId = 1L;

        given(animalRepository.findById(nonExistentAnimalId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> animalFavoriteService.toggleFavorite(nonExistentAnimalId, memberId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ANIMAL_NOT_FOUND);
    }

    @Test
    @DisplayName("내가 찜한 동물 목록을 페이징으로 조회할 수 있다")
    void getMyFavoriteAnimals_성공() {
        // given
        Long memberId = 1L;
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Animal> animalPage = new PageImpl<>(List.of(testAnimal), pageRequest, 1);

        given(memberRepository.existsById(memberId)).willReturn(true);
        given(animalFavoriteRepository.findFavoriteAnimalsByMemberId(memberId, pageRequest)).willReturn(animalPage);

        // when
        Page<AnimalResponse> result = animalFavoriteService.getMyFavoriteAnimals(memberId, pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).breed()).isEqualTo("시바견");
    }

    @Test
    @DisplayName("특정 동물의 찜 여부 및 찜 수를 확인할 수 있다")
    void isFavorite_and_count() {
        // given
        given(animalFavoriteRepository.existsByMemberIdAndAnimalId(1L, 1L)).willReturn(true);
        given(animalFavoriteRepository.countByAnimalId(1L)).willReturn(5L);

        // when & then
        assertThat(animalFavoriteService.isFavorite(1L, 1L)).isTrue();
        assertThat(animalFavoriteService.getFavoriteCount(1L)).isEqualTo(5L);
    }

    @Test
    @DisplayName("명시적으로 찜을 삭제할 수 있다 (removeFavorite)")
    void removeFavorite_성공() {
        // given
        Long animalId = 1L;
        Long memberId = 1L;
        AnimalFavorite existingFavorite = AnimalFavorite.builder()
                .member(testMember)
                .animal(testAnimal)
                .build();

        given(animalRepository.findById(animalId)).willReturn(Optional.of(testAnimal));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(testMember));
        given(animalFavoriteRepository.findByMemberAndAnimal(testMember, testAnimal)).willReturn(Optional.of(existingFavorite));
        given(animalFavoriteRepository.countByAnimalId(animalId)).willReturn(0L);

        // when
        FavoriteToggleResponseDto response = animalFavoriteService.removeFavorite(animalId, memberId);

        // then
        assertThat(response.isFavorite()).isFalse();
        assertThat(response.favoriteCount()).isEqualTo(0L);
        verify(animalFavoriteRepository).delete(existingFavorite);
    }
}
