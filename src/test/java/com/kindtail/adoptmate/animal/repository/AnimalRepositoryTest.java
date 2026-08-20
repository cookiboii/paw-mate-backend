package com.kindtail.adoptmate.animal.repository;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Gender;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AnimalRepositoryTest {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 테스트용 멤버 저장
        testMember = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .role(Role.USER)
                .build();
        memberRepository.save(testMember);
    }

    @Test
    @DisplayName("동물을 저장할 수 있다")
    void save_성공 () {
        // given
        Animal animal = Animal.builder()
                .species("강아지")
                .breed("진도개")
                .color("황색")
                .gender(Gender.MALE)
                .age(3L)
                .image("http://example.com/image.jpg")
                .status(Status.PROTECTED)
                .member(testMember)
                .build();

        // when
        Animal savedAnimal = animalRepository.save(animal);

        // then
        assertThat(savedAnimal.getId()).isNotNull();
        assertThat(savedAnimal.getSpecies()).isEqualTo("강아지");
        assertThat(savedAnimal.getMember().getId()).isEqualTo(testMember.getId());
    }

    @Test
    @DisplayName("ID 로 동물을 조회할 수 있다")
    void findById_성공 () {
        // given
        Animal animal = Animal.builder()
                .species("고양이")
                .breed("페르시안")
                .color("흰색")
                .gender(Gender.FEMALE)
                .age(2L)
                .member(testMember)
                .build();
        animalRepository.save(animal);

        // when
        Optional<Animal> foundAnimal = animalRepository.findById(animal.getId());

        // then
        assertThat(foundAnimal).isPresent();
        assertThat(foundAnimal.get().getSpecies()).isEqualTo("고양이");
        assertThat(foundAnimal.get().getBreed()).isEqualTo("페르시안");
    }

    @Test
    @DisplayName("존재하지 않는 ID 로 조회하면 빈 Optional 을 반환한다")
    void findById_없음 () {
        // when
        Optional<Animal> foundAnimal = animalRepository.findById(999L);

        // then
        assertThat(foundAnimal).isEmpty();
    }

    @Test
    @DisplayName("페이지네이션으로 동물을 조회할 수 있다")
    void findAll_페이지네이션_성공 () {
        // given
        for (int i = 0; i < 15; i++) {
            Animal animal = Animal.builder()
                    .species("강아지" + i)
                    .breed("진도개")
                    .color("황색")
                    .gender(Gender.MALE)
                    .age((long) i)
                    .member(testMember)
                    .build();
            animalRepository.save(animal);
        }

        // when
        Page<Animal> animals = animalRepository.findAll(PageRequest.of(0, 10));

        // then
        assertThat(animals.getTotalElements()).isEqualTo(15);
        assertThat(animals.getContent().size()).isEqualTo(10);
        assertThat(animals.isFirst()).isTrue();
    }

    @Test
    @DisplayName("종별로 동물을 페이지네이션 조회할 수 있다")
    void findBySpecies_페이지네이션_성공 () {
        // given
        for (int i = 0; i < 5; i++) {
            Animal dog = Animal.builder()
                    .species("강아지")
                    .breed("진도개")
                    .color("황색")
                    .gender(Gender.MALE)
                    .age((long) i)
                    .member(testMember)
                    .build();
            animalRepository.save(dog);
        }
        for (int i = 0; i < 3; i++) {
            Animal cat = Animal.builder()
                    .species("고양이")
                    .breed("페르시안")
                    .color("흰색")
                    .gender(Gender.FEMALE)
                    .age((long) i)
                    .member(testMember)
                    .build();
            animalRepository.save(cat);
        }

        // when
        Page<Animal> dogs = animalRepository.findBySpecies("강아지", PageRequest.of(0, 10));
        Page<Animal> cats = animalRepository.findBySpecies("고양이", PageRequest.of(0, 10));

        // then
        assertThat(dogs.getTotalElements()).isEqualTo(5);
        assertThat(cats.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("동물을 삭제할 수 있다")
    void deleteById_성공 () {
        // given
        Animal animal = Animal.builder()
                .species("강아지")
                .breed("푸들")
                .color("흰색")
                .gender(Gender.FEMALE)
                .age(1L)
                .member(testMember)
                .build();
        Animal savedAnimal = animalRepository.save(animal);

        // when
        animalRepository.deleteById(savedAnimal.getId());

        // then
        Optional<Animal> deletedAnimal = animalRepository.findById(savedAnimal.getId());
        assertThat(deletedAnimal).isEmpty();
    }

    @Test
    @DisplayName("deleteAnimalById 로 동물을 삭제할 수 있다")
    void deleteAnimalById_성공 () {
        // given
        Animal animal = Animal.builder()
                .species("강아지")
                .breed("비글")
                .color("갈색")
                .gender(Gender.MALE)
                .age(4L)
                .member(testMember)
                .build();
        Animal savedAnimal = animalRepository.save(animal);

        // when
        animalRepository.deleteAnimalById(savedAnimal.getId());

        // then
        Optional<Animal> foundAnimal = animalRepository.findById(savedAnimal.getId());
        assertThat(foundAnimal).isEmpty();
    }
}
