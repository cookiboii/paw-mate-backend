package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@SpringBootTest
@ActiveProfiles("test")
class SessionVsJwtBenchmarkTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 인메모리 세션 저장소
    private final Map<String, TokenUserInfo> inMemorySessionStore = new ConcurrentHashMap<>();

    private String sampleJwtToken;
    private String sampleSessionId;
    private TokenUserInfo sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = TokenUserInfo.builder()
                .email("bench_user@adoptmate.com")
                .role(Role.USER)
                .build();

        // 1. JWT 토큰 발급
        sampleJwtToken = jwtTokenProvider.createToken(sampleUser.getEmail(), sampleUser.getRole().name());

        // 2. 인메모리 세션 등록
        sampleSessionId = "SESSION_" + UUID.randomUUID();
        inMemorySessionStore.put(sampleSessionId, sampleUser);

        // 3. DB 세션 테이블 생성 및 세션 레코드 삽입 (Spring Session JDBC / RDB 세션 모델)
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_db_session (" +
                "session_id VARCHAR(100) PRIMARY KEY, " +
                "email VARCHAR(100), " +
                "role VARCHAR(50), " +
                "last_accessed_time TIMESTAMP)");
        jdbcTemplate.update("MERGE INTO test_db_session KEY (session_id) VALUES (?, ?, ?, NOW())",
                sampleSessionId, sampleUser.getEmail(), sampleUser.getRole().name());
    }

    @Test
    @DisplayName("[1] 단일 스레드 연속 인증 검증 속도 비교: JWT vs DB Session vs In-Memory Session")
    void compareSequentialPerformance() {
        int iterations = 10_000;

        // Warm-up
        for (int i = 0; i < 100; i++) {
            jwtTokenProvider.validateAndTokenUserInfo(sampleJwtToken);
            inMemorySessionStore.get(sampleSessionId);
            queryDbSession(sampleSessionId);
        }

        // 1. JWT 서명 검증 및 파싱 (Stateless, CPU Bound)
        StopWatch jwtWatch = new StopWatch();
        jwtWatch.start();
        for (int i = 0; i < iterations; i++) {
            TokenUserInfo info = jwtTokenProvider.validateAndTokenUserInfo(sampleJwtToken);
        }
        jwtWatch.stop();
        long jwtTotalMs = jwtWatch.getTotalTimeMillis();

        // 2. DB 세션 SQL 쿼리 조회 (Stateful, DB Connection & Disk I/O Bound)
        StopWatch dbWatch = new StopWatch();
        dbWatch.start();
        for (int i = 0; i < iterations; i++) {
            TokenUserInfo info = queryDbSession(sampleSessionId);
        }
        dbWatch.stop();
        long dbTotalMs = dbWatch.getTotalTimeMillis();

        // 3. In-Memory Session 조회 (Stateful, Heap Memory Bound)
        StopWatch memWatch = new StopWatch();
        memWatch.start();
        for (int i = 0; i < iterations; i++) {
            TokenUserInfo info = inMemorySessionStore.get(sampleSessionId);
        }
        memWatch.stop();
        long memTotalMs = memWatch.getTotalTimeMillis();

        System.out.println("\n==========================================================================================");
        System.out.println(" 📊 [단일 스레드 연속 인증 검증 속도 비교 (반복 횟수: " + iterations + "회)]");
        System.out.println("==========================================================================================");
        System.out.printf("1. JWT 서명 검증 (Stateless/CPU) : 총 %d ms | 1회당 약 %.4f ms | TPS: %.0f req/s%n",
                jwtTotalMs, (double) jwtTotalMs / iterations, (double) iterations / (jwtTotalMs / 1000.0));
        System.out.printf("2. DB 세션 SQL 조회 (RDB/I/O)    : 총 %d ms | 1회당 약 %.4f ms | TPS: %.0f req/s%n",
                dbTotalMs, (double) dbTotalMs / iterations, (double) iterations / (dbTotalMs / 1000.0));
        System.out.printf("3. In-Memory 세션 (Heap)         : 총 %d ms | 1회당 약 %.4f ms | TPS: %.0f req/s%n",
                memTotalMs, (double) memTotalMs / iterations, (double) iterations / (memTotalMs / 1000.0));
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("💡 [비교 결과] JWT vs DB 세션 속도 차이: JWT가 DB 세션 대비 약 %.2f 배 더 빠름 / 느림%n",
                (double) dbTotalMs / jwtTotalMs);
        System.out.println("==========================================================================================\n");
    }

    @Test
    @DisplayName("[2] 멀티스레드 동시 인증 부하 비교: 50개 스레드 동시 요청 (DB Connection Pool 경합 vs JWT)")
    void compareConcurrentPerformance() throws InterruptedException {
        int threadCount = 50;
        int requestsPerThread = 100;
        int totalRequests = threadCount * requestsPerThread; // 5,000 요청

        // 1. JWT 멀티스레드 부하 테스트 (Zero Lock, Zero I/O)
        long jwtElapsed = runConcurrentTask(threadCount, requestsPerThread, () -> {
            jwtTokenProvider.validateAndTokenUserInfo(sampleJwtToken);
        });

        // 2. DB 세션 멀티스레드 부하 테스트 (HikariCP 커넥션 풀 경합 + DB 락)
        long dbElapsed = runConcurrentTask(threadCount, requestsPerThread, () -> {
            queryDbSession(sampleSessionId);
        });

        // 3. In-Memory 세션 멀티스레드 부하 테스트
        long memElapsed = runConcurrentTask(threadCount, requestsPerThread, () -> {
            inMemorySessionStore.get(sampleSessionId);
        });

        System.out.println("\n==========================================================================================");
        System.out.println(" ⚡ [멀티스레드 동시성 인증 부하 비교 (50개 동시 스레드 / 총 " + totalRequests + "건)]");
        System.out.println("==========================================================================================");
        System.out.printf("1. JWT 서명 검증 (Stateless)     : 소요 시간 %d ms | TPS: %.0f req/s%n",
                jwtElapsed, (double) totalRequests / (jwtElapsed / 1000.0));
        System.out.printf("2. DB 세션 조회 (HikariCP 경합) : 소요 시간 %d ms | TPS: %.0f req/s%n",
                dbElapsed, (double) totalRequests / (dbElapsed / 1000.0));
        System.out.printf("3. In-Memory 세션 (단일 서버)   : 소요 시간 %d ms | TPS: %.0f req/s%n",
                memElapsed, (double) totalRequests / (memElapsed / 1000.0));
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("💡 [동시성 분석] DB 커넥션 풀 경합 시: JWT가 DB 세션 대비 약 %.2f 배 더 높은 처리량(Throughput) 달성%n",
                (double) dbElapsed / jwtElapsed);
        System.out.println("==========================================================================================\n");
    }

    @Test
    @DisplayName("[3] 네트워크 페이로드 및 대역폭(Bandwidth) 비교 분석")
    void comparePayloadAndBandwidth() {
        int sessionIdBytes = ("JSESSIONID=" + sampleSessionId).getBytes().length;
        int jwtBytes = ("Authorization: Bearer " + sampleJwtToken).getBytes().length;

        long oneMillion = 1_000_000L;
        double sessionMbPerMillion = (double) (sessionIdBytes * oneMillion) / (1024 * 1024);
        double jwtMbPerMillion = (double) (jwtBytes * oneMillion) / (1024 * 1024);

        System.out.println("\n==========================================================================================");
        System.out.println(" 📦 [네트워크 페이로드 크기 및 대역폭 소모량 비교]");
        System.out.println("==========================================================================================");
        System.out.printf("• Session 쿠키 헤더 크기 : %d Bytes%n", sessionIdBytes);
        System.out.printf("• JWT Bearer 헤더 크기   : %d Bytes (세션 대비 %.1f배 크기)%n", jwtBytes, (double) jwtBytes / sessionIdBytes);
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("• 100만 회 요청 시 Session 대역폭 전송량 : %.2f MB%n", sessionMbPerMillion);
        System.out.printf("• 100만 회 요청 시 JWT 대역폭 전송량     : %.2f MB (추가 대역폭: +%.2f MB)%n",
                jwtMbPerMillion, jwtMbPerMillion - sessionMbPerMillion);
        System.out.println("==========================================================================================\n");
    }

    private TokenUserInfo queryDbSession(String sessionId) {
        return jdbcTemplate.queryForObject(
                "SELECT email, role FROM test_db_session WHERE session_id = ?",
                (rs, rowNum) -> TokenUserInfo.builder()
                        .email(rs.getString("email"))
                        .role(Role.valueOf(rs.getString("role")))
                        .build(),
                sessionId
        );
    }

    private long runConcurrentTask(int threadCount, int requestsPerThread, Runnable task) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < requestsPerThread; i++) {
                        task.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        startLatch.countDown();
        endLatch.await(30, TimeUnit.SECONDS);
        stopWatch.stop();

        executor.shutdown();
        return stopWatch.getTotalTimeMillis();
    }
}
