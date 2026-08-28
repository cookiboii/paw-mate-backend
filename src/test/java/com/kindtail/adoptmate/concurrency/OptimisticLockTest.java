package com.kindtail.adoptmate.concurrency;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OptimisticLockTest {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Animal animal;
    private Member member;

    @BeforeEach
    void setUp() {
        animalRepository.deleteAll();
        postRepository.deleteAll();
        memberRepository.deleteAll();

        member = memberRepository.save(
                Member.builder()
                        .email("owner@pawmate.com")
                        .name("작성자")
                        .password("1234")
                        .role(Role.USER)
                        .build()
        );

        animal = animalRepository.save(
                Animal.builder()
                        .species(Species.CAT)
                        .breed("코리안 숏헤어")
                        .status(Status.PROTECTED)
                        .age(1L)
                        .color("치즈")
                        .member(member)
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        animalRepository.deleteAll();
        postRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("동일 동물 엔티티를 두 트랜잭션이 동시 수정 시 낙관적 락 충돌로 1건은 실패한다")
    void animal_OptimisticLock_ConflictDetection() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readLatch = new CountDownLatch(2);
        CountDownLatch startCommitLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger optimisticLockExceptionCount = new AtomicInteger(0);

        TransactionTemplate tx1 = new TransactionTemplate(transactionManager);
        tx1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionTemplate tx2 = new TransactionTemplate(transactionManager);
        tx2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // when: Thread 1
        executorService.submit(() -> {
            try {
                tx1.execute(status -> {
                    Animal a1 = animalRepository.findById(animal.getId()).orElseThrow();
                    readLatch.countDown(); // Thread 1 조회 완료

                    try {
                        startCommitLatch.await(); // Thread 2도 조회할 때까지 대기
                    } catch (InterruptedException ignored) {
                    }

                    a1.updateStatus(new AnimalStatusUpdateRequest(Status.WAITING));
                    animalRepository.saveAndFlush(a1);
                    return null;
                });
                successCount.incrementAndGet();
            } catch (Exception e) {
                if (e instanceof ObjectOptimisticLockingFailureException || e.getCause() instanceof jakarta.persistence.OptimisticLockException) {
                    optimisticLockExceptionCount.incrementAndGet();
                }
            } finally {
                doneLatch.countDown();
            }
        });

        // when: Thread 2
        executorService.submit(() -> {
            try {
                tx2.execute(status -> {
                    Animal a2 = animalRepository.findById(animal.getId()).orElseThrow();
                    readLatch.countDown(); // Thread 2 조회 완료

                    try {
                        startCommitLatch.await();
                        Thread.sleep(50); // Thread 1이 먼저 커밋하도록 살짝 지연
                    } catch (InterruptedException ignored) {
                    }

                    a2.updateStatus(new AnimalStatusUpdateRequest(Status.ADOPTED));
                    animalRepository.saveAndFlush(a2);
                    return null;
                });
                successCount.incrementAndGet();
            } catch (Exception e) {
                if (e instanceof ObjectOptimisticLockingFailureException || e.getCause() instanceof jakarta.persistence.OptimisticLockException) {
                    optimisticLockExceptionCount.incrementAndGet();
                }
            } finally {
                doneLatch.countDown();
            }
        });

        readLatch.await();         // 두 스레드 모두 초기 데이터(version=0) 조회 완료
        startCommitLatch.countDown(); // 동시 커밋 경쟁 시작
        doneLatch.await();
        executorService.shutdown();

        // then
        Animal finalAnimal = animalRepository.findById(animal.getId()).orElseThrow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(optimisticLockExceptionCount.get()).isEqualTo(1);
        assertThat(finalAnimal.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("동일 게시글을 두 트랜잭션이 동시 수정 시 낙관적 락이 충돌을 감지한다")
    void post_OptimisticLock_ConflictDetection() throws InterruptedException {
        // given
        Post post = postRepository.save(
                Post.builder()
                        .title("초기 제목")
                        .content("초기 내용")
                        .member(member)
                        .build()
        );

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readLatch = new CountDownLatch(2);
        CountDownLatch startCommitLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger optimisticLockExceptionCount = new AtomicInteger(0);

        TransactionTemplate tx1 = new TransactionTemplate(transactionManager);
        tx1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionTemplate tx2 = new TransactionTemplate(transactionManager);
        tx2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // when: Thread 1
        executorService.submit(() -> {
            try {
                tx1.execute(status -> {
                    Post p1 = postRepository.findById(post.getId()).orElseThrow();
                    readLatch.countDown();

                    try {
                        startCommitLatch.await();
                    } catch (InterruptedException ignored) {
                    }

                    p1.updatePost("스레드1 수정 제목", "스레드1 내용", null);
                    postRepository.saveAndFlush(p1);
                    return null;
                });
                successCount.incrementAndGet();
            } catch (Exception e) {
                if (e instanceof ObjectOptimisticLockingFailureException || e.getCause() instanceof jakarta.persistence.OptimisticLockException) {
                    optimisticLockExceptionCount.incrementAndGet();
                }
            } finally {
                doneLatch.countDown();
            }
        });

        // when: Thread 2
        executorService.submit(() -> {
            try {
                tx2.execute(status -> {
                    Post p2 = postRepository.findById(post.getId()).orElseThrow();
                    readLatch.countDown();

                    try {
                        startCommitLatch.await();
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }

                    p2.updatePost("스레드2 수정 제목", "스레드2 내용", null);
                    postRepository.saveAndFlush(p2);
                    return null;
                });
                successCount.incrementAndGet();
            } catch (Exception e) {
                if (e instanceof ObjectOptimisticLockingFailureException || e.getCause() instanceof jakarta.persistence.OptimisticLockException) {
                    optimisticLockExceptionCount.incrementAndGet();
                }
            } finally {
                doneLatch.countDown();
            }
        });

        readLatch.await();
        startCommitLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        Post finalPost = postRepository.findById(post.getId()).orElseThrow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(optimisticLockExceptionCount.get()).isEqualTo(1);
        assertThat(finalPost.getVersion()).isEqualTo(1L);
    }
}
