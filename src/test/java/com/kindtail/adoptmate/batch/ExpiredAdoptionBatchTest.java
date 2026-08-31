package com.kindtail.adoptmate.batch;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.domain.HousingType;
import com.kindtail.adoptmate.adoption.repository.AdoptionRepository;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Gender;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class ExpiredAdoptionBatchTest {

    @Autowired(required = false)
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("expiredAdoptionJob")
    private Job expiredAdoptionJob;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private AdoptionRepository adoptionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member testMember;
    private Animal expiredAnimal;
    private Animal activeAnimal;
    private Adoption expiredAdoption;
    private Adoption recentAdoption;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(expiredAdoptionJob);

        // 1. 테스트 회원 생성
        testMember = memberRepository.save(
                Member.builder()
                        .name("테스터")
                        .email("batch_test@test.com")
                        .role(Role.USER)
                        .build()
        );

        // 2. 동물 2마리 생성 (신청 접수 상태인 WAITING)
        expiredAnimal = animalRepository.save(
                Animal.builder()
                        .species(Species.DOG)
                        .gender(Gender.MALE)
                        .breed("말티즈")
                        .status(Status.WAITING)
                        .member(testMember)
                        .build()
        );

        activeAnimal = animalRepository.save(
                Animal.builder()
                        .species(Species.CAT)
                        .gender(Gender.FEMALE)
                        .breed("코숏")
                        .status(Status.WAITING)
                        .member(testMember)
                        .build()
        );

        // 3. 20일 전 신청된 만료 대상 입양 건 (PENDING)
        expiredAdoption = adoptionRepository.save(
                Adoption.builder()
                        .member(testMember)
                        .animal(expiredAnimal)
                        .phone("010-1234-5678")
                        .housingType(HousingType.APARTMENT)
                        .hasPet("없음")
                        .reason("오래된 입양 신청")
                        .status(AdoptionStatus.PENDING)
                        .build()
        );
        jdbcTemplate.update("UPDATE adoption SET apply_date = ? WHERE adoption_id = ?",
                LocalDateTime.now().minusDays(20), expiredAdoption.getId());

        // 4. 2일 전 신청된 최신 입양 건 (PENDING)
        recentAdoption = adoptionRepository.save(
                Adoption.builder()
                        .member(testMember)
                        .animal(activeAnimal)
                        .phone("010-9876-5432")
                        .housingType(HousingType.VILLA)
                        .hasPet("없음")
                        .reason("최근 입양 신청")
                        .status(AdoptionStatus.PENDING)
                        .build()
        );
        jdbcTemplate.update("UPDATE adoption SET apply_date = ? WHERE adoption_id = ?",
                LocalDateTime.now().minusDays(2), recentAdoption.getId());
    }

    @Test
    @DisplayName("14일 이상 경과한 PENDING 입양 신청은 REJECTED로 변경되고, 동물 상태는 PROTECTED로 복구된다")
    void executeExpiredAdoptionJob() throws Exception {
        // given
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(14);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("thresholdDate", thresholdDate.toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 1. 20일 전 신청 건 검증: REJECTED로 자동 반려 처리됨
        Adoption updatedExpiredAdoption = adoptionRepository.findById(expiredAdoption.getId()).orElseThrow();
        assertThat(updatedExpiredAdoption.getStatus()).isEqualTo(AdoptionStatus.REJECTED);

        // 2. 만료된 신청의 동물 검증: WAITING -> PROTECTED(입양 가능) 상태로 복구됨
        Animal updatedExpiredAnimal = animalRepository.findById(expiredAnimal.getId()).orElseThrow();
        assertThat(updatedExpiredAnimal.getStatus()).isEqualTo(Status.PROTECTED);

        // 3. 2일 전 신청 건 검증: 여전히 PENDING 상태 유지
        Adoption updatedRecentAdoption = adoptionRepository.findById(recentAdoption.getId()).orElseThrow();
        assertThat(updatedRecentAdoption.getStatus()).isEqualTo(AdoptionStatus.PENDING);

        // 4. 최근 신청의 동물 검증: 여전히 WAITING 상태 유지
        Animal updatedActiveAnimal = animalRepository.findById(activeAnimal.getId()).orElseThrow();
        assertThat(updatedActiveAnimal.getStatus()).isEqualTo(Status.WAITING);
    }
}
