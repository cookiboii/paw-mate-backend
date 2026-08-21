package com.kindtail.adoptmate.animal.domain;

import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnimalTest {

    @Test
    @DisplayName("Animal 엔티티 빌더로 생성된다")
    void 동물_엔티티_빌더로_생성 () {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .role(Role.USER)
                .build();

        // when
        Animal animal = Animal.builder()
                .species(Species.DOG)
                .breed("진도개")
                .color("황색")
                .gender(Gender.MALE)
                .age(3L)
                .image("http://example.com/image.jpg")
                .status(Status.PROTECTED)
                .member(member)
                .build();

        // then
        assertThat(animal.getSpecies()).isEqualTo(Species.DOG);
        assertThat(animal.getBreed()).isEqualTo("진도개");
        assertThat(animal.getColor()).isEqualTo("황색");
        assertThat(animal.getGender()).isEqualTo(Gender.MALE);
        assertThat(animal.getAge()).isEqualTo(3L);
        assertThat(animal.getImage()).isEqualTo("http://example.com/image.jpg");
        assertThat(animal.getStatus()).isEqualTo(Status.PROTECTED);
        assertThat(animal.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("Animal 의 기본 상태는 PROTECTED 이다")
    void 동물_엔티티_기본_상태는_PROTECTED () {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .role(Role.USER)
                .build();

        // when
        Animal animal = Animal.builder()
                .species(Species.DOG)
                .breed("시바견")
                .color("갈색")
                .gender(Gender.FEMALE)
                .age(2L)
                .member(member)
                .build();

        // then
        assertThat(animal.getStatus()).isEqualTo(Status.PROTECTED);
    }

    @Test
    @DisplayName("updateStatus 로 동물 상태를 변경할 수 있다")
    void updateStatus_로_동물_상태를_변경할_수_있다 () {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .role(Role.USER)
                .build();

        Animal animal = Animal.builder()
                .species(Species.DOG)
                .breed("푸들")
                .color("흰색")
                .gender(Gender.FEMALE)
                .age(1L)
                .member(member)
                .status(Status.WAITING)
                .build();

        AnimalStatusUpdateRequest request = new AnimalStatusUpdateRequest(Status.ADOPTED);

        // when
        animal.updateStatus(request);

        // then
        assertThat(animal.getStatus()).isEqualTo(Status.ADOPTED);
    }
}
