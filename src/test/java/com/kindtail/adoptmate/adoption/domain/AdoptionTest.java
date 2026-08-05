package com.kindtail.adoptmate.adoption.domain;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class AdoptionTest {

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
                .species("강아지")
                .status(Status.PROTECTED)
                .image("test.jpg")
                .build();
    }

    @Test
    @DisplayName("Adoption.of 정적 팩토리 메서드로 입양 신청을 생성할 수 있다")
    void createAdoptionWithStaticFactoryMethod() {
        // given
        String interview = "안녕하세요, 저는 반려동물을 키울 준비가 되어 있습니다.";

        // when
        Adoption adoption = Adoption.of(member, animal, interview, AdoptionStatus.PENDING);

        // then
        assertThat(adoption).isNotNull();
        assertThat(adoption.getMember()).isEqualTo(member);
        assertThat(adoption.getAnimal()).isEqualTo(animal);
        assertThat(adoption.getInterview()).isEqualTo(interview);
        assertThat(adoption.getStatus()).isEqualTo(AdoptionStatus.PENDING);
        assertThat(adoption.getApplyDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("updateAdoption 메서드로 입양 상태를 변경할 수 있다")
    void updateAdoptionStatus() {
        // given
        Adoption adoption = Adoption.of(member, animal, "인터뷰 내용", AdoptionStatus.PENDING);

        // when
        adoption.updateAdoption(AdoptionStatus.APPROVED);

        // then
        assertThat(adoption.getStatus()).isEqualTo(AdoptionStatus.APPROVED);
    }

    @Test
    @DisplayName("입양 신청 시 현재 시간이 applyDate 로 설정된다")
    void applyDateIsSetToCurrentTime() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        Adoption adoption = Adoption.of(member, animal, "인터뷰", AdoptionStatus.PENDING);

        // then
        assertThat(adoption.getApplyDate()).isAfterOrEqualTo(before);
        assertThat(adoption.getApplyDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
