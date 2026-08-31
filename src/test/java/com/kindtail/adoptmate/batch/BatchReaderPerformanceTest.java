package com.kindtail.adoptmate.batch;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

class BatchReaderPerformanceTest {

    @Autowired(required = false)
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DummyDataGenerator dummyDataGenerator;

    @Autowired
    @Qualifier("offsetJob")
    private Job offsetJob;

    @Autowired
    @Qualifier("zeroOffsetJob")
    private Job zeroOffsetJob;

    private static final int DATA_COUNT = 100_000; // 10만, 50만, 100만 조절 가능

    @BeforeAll
    void setupData() {
        System.out.println("=== 더미 데이터 " + DATA_COUNT + "건 삽입 시작 ===");
        dummyDataGenerator.insertDummyAdoptions(DATA_COUNT);
        System.out.println("=== 더미 데이터 삽입 완료 ===");
    }

    @Test
    @DisplayName("Limit-Offset vs Zero-Offset 3회 반복 평균 성능 측정")
    void comparePerformance() throws Exception {
        int repeatCount = 3;

        long totalOffsetDuration = runBenchmark(offsetJob, repeatCount, "Limit-Offset (JpaPagingItemReader)");
        long totalZeroOffsetDuration = runBenchmark(zeroOffsetJob, repeatCount, "Zero-Offset (WHERE id > lastId)");

        System.out.println("\n==========================================");
        System.out.println("데이터 건수: " + DATA_COUNT);
        System.out.printf("Limit-Offset 평균 소요 시간: %d ms%n", (totalOffsetDuration / repeatCount));
        System.out.printf("Zero-Offset  평균 소요 시간: %d ms%n", (totalZeroOffsetDuration / repeatCount));
        System.out.printf("성능 개선율: %.2f 배 빨라짐%n", (double) totalOffsetDuration / totalZeroOffsetDuration);
        System.out.println("==========================================");
    }

    private long runBenchmark(Job job, int repeatCount, String jobName) throws Exception {
        jobLauncherTestUtils.setJob(job);
        long totalDuration = 0;

        System.out.println("\n>>> [" + jobName + "] 벤치마크 시작 <<<");

        for (int i = 1; i <= repeatCount; i++) {
            System.gc(); // 이전 테스트 잔여 객체 정리
            Thread.sleep(1000);

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addLong("iteration", (long) i)
                    .toJobParameters();

            JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
            stopWatch.stop();

            long elapsed = stopWatch.getTotalTimeMillis();
            totalDuration += elapsed;

            System.out.printf("  %d회차 실행 - 소요 시간: %d ms | 상태: %s%n", 
                    i, elapsed, jobExecution.getStatus());
        }
        return totalDuration;
    }
}
