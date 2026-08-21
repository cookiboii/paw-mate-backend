package com.kindtail.adoptmate.adoption.repository;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AdoptionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AdoptionRepository adoptionRepository;

    private Member member;
    private Animal animal;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .name("홍길동")
                .email("test@example.com")
                .role(Role.USER)
                .build();
        entityManager.persist(member);

        animal = Animal.builder()
                .species(Species.DOG)
                .status(Status.PROTECTED)
                .image("test.jpg")
                .member(member)
                .build();
        entityManager.persist(animal);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("입양 신청을 저장할 수 있다")
    void saveAdoption() {
        // given
        Adoption adoption = Adoption.of(member, animal, "인터뷰 내용", AdoptionStatus.PENDING);

        // when
        Adoption saved = adoptionRepository.save(adoption);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMember().getName()).isEqualTo("홍길동");
        assertThat(saved.getAnimal().getSpecies()).isEqualTo(Species.DOG);
        assertThat(saved.getStatus()).isEqualTo(AdoptionStatus.PENDING);
    }

    @Test
    @DisplayName("회원과 동물로 중복 입양 신청 여부를 확인할 수 있다")
    void existsByMemberAndAnimal() {
        // given
        Adoption adoption = Adoption.of(member, animal, "인터뷰", AdoptionStatus.PENDING);
        adoptionRepository.save(adoption);
        entityManager.flush();
        entityManager.clear();

        // when
        boolean exists = adoptionRepository.existsByMemberAndAnimal(member, animal);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 회원과 동물은 중복 신청이 없다")
    void existsByMemberAndAnimalNotFound() {
        // given
        Member newMember = Member.builder()
                .name("김철수")
                .email("new@example.com")
                .role(Role.USER)
                .build();
        entityManager.persist(newMember);
        entityManager.flush();
        entityManager.clear();

        // when
        boolean exists = adoptionRepository.existsByMemberAndAnimal(newMember, animal);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Fetch Join 으로 입양 신청과 회원, 동물 정보를 함께 조회할 수 있다")
    void findByIdWithFetchJoin() {
        // given
        Adoption adoption = Adoption.of(member, animal, "인터뷰", AdoptionStatus.PENDING);
        adoptionRepository.save(adoption);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Adoption> found = adoptionRepository.findByIdWithFetchJoin(adoption.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getMember().getName()).isEqualTo("홍길동");
        assertThat(found.get().getAnimal().getSpecies()).isEqualTo(Species.DOG);
    }

    @Test
    @DisplayName("회원의 모든 입양 신청을 Fetch Join 으로 조회할 수 있다")
    void findByMember() {
        // given
        Animal animal2 = Animal.builder()
                .species(Species.CAT)
                .status(Status.PROTECTED)
                .image("cat.jpg")
                .member(member)
                .build();
        entityManager.persist(animal2);

        Adoption adoption1 = Adoption.of(member, animal, "인터뷰 1", AdoptionStatus.PENDING);
        Adoption adoption2 = Adoption.of(member, animal2, "인터뷰 2", AdoptionStatus.APPROVED);
        adoptionRepository.save(adoption1);
        adoptionRepository.save(adoption2);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Adoption> adoptions = adoptionRepository.findByMember(member);

        // then
        assertThat(adoptions).hasSize(2);
        assertThat(adoptions.get(0).getInterview()).isEqualTo("인터뷰 1");
        assertThat(adoptions.get(1).getInterview()).isEqualTo("인터뷰 2");
    }

    @Test
    @DisplayName("모든 입양 신청을 Fetch Join 으로 조회할 수 있다")
    void findAllWithFetchJoin() {
        // given
        Animal animal2 = Animal.builder()
                .species(Species.CAT)
                .status(Status.PROTECTED)
                .image("cat.jpg")
                .member(member)
                .build();
        entityManager.persist(animal2);

        Adoption adoption1 = Adoption.of(member, animal, "인터뷰 1", AdoptionStatus.PENDING);
        Adoption adoption2 = Adoption.of(member, animal2, "인터뷰 2", AdoptionStatus.APPROVED);
        adoptionRepository.save(adoption1);
        adoptionRepository.save(adoption2);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Adoption> adoptions = adoptionRepository.findAllWithFetchJoin();

        // then
        assertThat(adoptions).hasSize(2);
    }

    @Test
    @DisplayName("EntityGraph 를 사용하여 페이지네이션과 함께 조회할 수 있다")
    void findAllWithEntityGraph() {
        // given
        for (int i = 0; i < 5; i++) {
            Animal a = Animal.builder()
                    .species(Species.ETC)
                    .status(Status.PROTECTED)
                    .image("animal" + i + ".jpg")
                    .member(member)
                    .build();
            entityManager.persist(a);
            Adoption adoption = Adoption.of(member, a, "인터뷰 " + i, AdoptionStatus.PENDING);
            adoptionRepository.save(adoption);
        }
        entityManager.flush();
        entityManager.clear();

        // when
        Page<Adoption> page = adoptionRepository.findAll(PageRequest.of(0, 3));

        // then
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("동일한 회원과 동일한 동물로 중복 입양 신청 시 DB Unique 제약조건 위반 예외가 발생한다")
    void duplicateAdoptionThrowsDataIntegrityViolationException() {
        // given
        Adoption adoption1 = Adoption.of(member, animal, "첫 번째 신청", AdoptionStatus.PENDING);
        adoptionRepository.save(adoption1);
        entityManager.flush();

        Adoption adoption2 = Adoption.of(member, animal, "두 번째 중복 신청", AdoptionStatus.PENDING);

        // when & then
        assertThatThrownBy(() -> {
            adoptionRepository.save(adoption2);
            entityManager.flush();
        }).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
