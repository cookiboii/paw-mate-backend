package com.kindtail.adoptmate.concurrency;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.domain.HousingType;
import com.kindtail.adoptmate.adoption.dto.AdoptionCreateRequest;
import com.kindtail.adoptmate.adoption.facade.AdoptionFacade;
import com.kindtail.adoptmate.adoption.repository.AdoptionRepository;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.dto.MemberRegisterRequestDto;
import com.kindtail.adoptmate.member.facade.MemberFacade;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DistributedLockTest {

    @Autowired
    private AdoptionFacade adoptionFacade;

    @Autowired
    private AdoptionRepository adoptionRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberFacade memberFacade;

    private Animal animal;
    private List<Member> members = new ArrayList<>();

    @BeforeEach
    void setUp() {
        adoptionRepository.deleteAll();
        animalRepository.deleteAll();
        memberRepository.deleteAll();

        // 동물 등록 (보호자 회원 필요)
        Member shelter = memberRepository.save(
                Member.builder()
                        .email("shelter@pawmate.com")
                        .name("보호소")
                        .password("1234")
                        .role(Role.ADMIN)
                        .build()
        );

        animal = animalRepository.save(
                Animal.builder()
                        .species(Species.DOG)
                        .breed("골든 리트리버")
                        .status(Status.PROTECTED)
                        .age(2L)
                        .color("황금색")
                        .member(shelter)
                        .build()
        );

        // 신청자 10명 미리 생성
        for (int i = 1; i <= 10; i++) {
            Member member = memberRepository.save(
                    Member.builder()
                            .email("applicant" + i + "@pawmate.com")
                            .name("신청자" + i)
                            .password("password123")
                            .role(Role.USER)
                            .build()
            );
            members.add(member);
        }
    }

    @AfterEach
    void tearDown() {
        adoptionRepository.deleteAll();
        animalRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("동일 동물에 대해 10명의 유저가 동시에 입양 신청 시 분산락에 의해 정확히 1명만 성공한다")
    void applyAdoption_Concurrency_SingleSuccess() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final Long memberId = members.get(i).getId();
            final Long animalId = animal.getId();

            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // 모든 스레드가 준비되면 동시에 출발

                    AdoptionCreateRequest request = new AdoptionCreateRequest(
                            "010-1234-5678",
                            HousingType.APARTMENT,
                            "없음",
                            "평생 책임지겠습니다."
                    );
                    adoptionFacade.applyAdoption(request, memberId, animalId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown(); // 동시 출발 신호
        doneLatch.await();      // 모든 스레드 작업 완료 대기
        executorService.shutdown();

        // then
        List<Adoption> adoptions = adoptionRepository.findAll();
        Animal updatedAnimal = animalRepository.findById(animal.getId()).orElseThrow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
        assertThat(adoptions).hasSize(1);
        assertThat(updatedAnimal.getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    @DisplayName("동일 이메일로 10번 동시 회원가입 요청 시 분산락에 의해 정확히 1건만 등록된다")
    void registerMember_Concurrency_SingleSuccess() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        String duplicateEmail = "concurrent_user@pawmate.com";

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();

                    MemberRegisterRequestDto dto = new MemberRegisterRequestDto(
                            "동시가입자",
                            duplicateEmail,
                            "password123!",
                            Role.USER
                    );
                    memberFacade.registerMember(dto);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
        assertThat(memberRepository.findByEmail(duplicateEmail)).isPresent();
    }
}
