package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.member.domain.Role;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class SessionVsJwtBenchmarkTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 인메모리 세션 저장소 (단일 서버 인메모리 세션 모델)
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
            assertThat(info.getEmail()).isEqualTo(sampleUser.getEmail());
        }
        jwtWatch.stop();
        long jwtTotalMs = jwtWatch.getTotalTimeMillis();

        // 2. DB 세션 SQL 쿼리 조회 (Stateful, DB Connection & Disk I/O Bound)
        StopWatch dbWatch = new StopWatch();
        dbWatch.start();
        for (int i = 0; i < iterations; i++) {
            TokenUserInfo info = queryDbSession(sampleSessionId);
            assertThat(info.getEmail()).isEqualTo(sampleUser.getEmail());
        }
        dbWatch.stop();
        long dbTotalMs = dbWatch.getTotalTimeMillis();

        // 3. In-Memory Session 조회 (Stateful, Heap Memory Bound)
        StopWatch memWatch = new StopWatch();
        memWatch.start();
        for (int i = 0; i < iterations; i++) {
            TokenUserInfo info = inMemorySessionStore.get(sampleSessionId);
            assertThat(info.getEmail()).isEqualTo(sampleUser.getEmail());
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
        System.out.println("💡 [분석 노트]");
        System.out.println("• H2 인메모리 DB는 로컬 JVM 힙 메모리 참조로 네트워크 RTT/디스크 I/O가 없어 비정상적으로 빠릅니다.");
        System.out.println("• 실제 운영 환경(외부 MySQL/Postgres 등)에서는 1회 쿼리당 1~5ms 이상의 네트워크 지연이 발생하여");
        System.out.println("  1만 회 조회 시 DB 세션은 약 10,000~50,000ms가 소요되며 커넥션 풀을 독점하게 됩니다.");
        System.out.println("• 반면 JWT는 외부 네트워크 I/O나 DB 커넥션 소모 없이 WAS CPU 암호화 연산만으로 완전히 독립 수행됩니다.");
        System.out.println("==========================================================================================\n");
    }

    @Test
    @DisplayName("[2] 멀티스레드 동시 인증 부하 비교: 50개 스레드 동시 요청 (DB Connection Pool 경합 vs JWT)")
    void compareConcurrentPerformance() throws InterruptedException {
        int threadCount = 50;
        int requestsPerThread = 100;
        int totalRequests = threadCount * requestsPerThread; // 5,000 요청

        // 1. JWT 멀티스레드 부하 테스트 (Zero Lock, Zero DB Connection)
        long jwtElapsed = runConcurrentTask(threadCount, requestsPerThread, () -> {
            TokenUserInfo info = jwtTokenProvider.validateAndTokenUserInfo(sampleJwtToken);
            assertThat(info.getEmail()).isEqualTo(sampleUser.getEmail());
        });

        // 2. DB 세션 멀티스레드 부하 테스트 (HikariCP 커넥션 풀 경합 + DB 커넥션 점유)
        long dbElapsed = runConcurrentTask(threadCount, requestsPerThread, () -> {
            TokenUserInfo info = queryDbSession(sampleSessionId);
            assertThat(info.getEmail()).isEqualTo(sampleUser.getEmail());
        });

        // 3. In-Memory 세션 멀티스레드 부하 테스트
        long memElapsed = runConcurrentTask(threadCount, requestsPerThread, () -> {
            TokenUserInfo info = inMemorySessionStore.get(sampleSessionId);
            assertThat(info.getEmail()).isEqualTo(sampleUser.getEmail());
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
        System.out.println("💡 [동시성 분석]");
        System.out.println("• DB 세션은 요청마다 HikariCP 커넥션을 획득/반납해야 하므로 트래픽 폭증 시 커넥션 풀 고갈의 원인이 됩니다.");
        System.out.println("• JWT는 DB 커넥션을 단 1개도 사용하지 않아(Zero-I/O), 비즈니스 로직(입양/댓글/게시글) 처리에 커넥션을 온전히 양보합니다.");
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

        assertThat(jwtBytes).isGreaterThan(sessionIdBytes);

        System.out.println("\n==========================================================================================");
        System.out.println(" 📦 [네트워크 페이로드 크기 및 대역폭 소모량 비교]");
        System.out.println("==========================================================================================");
        System.out.printf("• Session 쿠키 헤더 크기 : %d Bytes%n", sessionIdBytes);
        System.out.printf("• JWT Bearer 헤더 크기   : %d Bytes (세션 대비 약 %.1f배 크기)%n", jwtBytes, (double) jwtBytes / sessionIdBytes);
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("• 100만 회 요청 시 Session 대역폭 전송량 : %.2f MB%n", sessionMbPerMillion);
        System.out.printf("• 100만 회 요청 시 JWT 대역폭 전송량     : %.2f MB (추가 전송량: +%.2f MB)%n",
                jwtMbPerMillion, jwtMbPerMillion - sessionMbPerMillion);
        System.out.println("💡 [대역폭 트레이드오프] JWT는 무상태성을 얻는 대신 클라이언트-서버 간 전송 페이로드가 상대적으로 큽니다.");
        System.out.println("==========================================================================================\n");
    }

    @Test
    @DisplayName("[4] 다중 서버 확장성(Scale-out) 시뮬레이션: 세션(Sticky / 외부 저장소 종속) vs JWT(완전 무상태 확장성)")
    void compareScaleOutCharacteristics() {
        // 클라우드 환경의 다중 서버 인스턴스 (Node A, Node B) 가정
        Map<String, TokenUserInfo> serverNodeA = new ConcurrentHashMap<>();
        Map<String, TokenUserInfo> serverNodeB = new ConcurrentHashMap<>();

        // 1. Session 방식: Node A에서 사용자가 로그인하여 로컬 세션 생성
        String sessionId = "SESSION_" + UUID.randomUUID();
        serverNodeA.put(sessionId, sampleUser);

        // Node A로 요청 시 세션 조회 성공
        assertThat(serverNodeA.get(sessionId)).isNotNull();
        // 로드밸런서가 Node B로 요청을 라우팅하면 세션이 없으므로 인증 실패 발생!
        // (Sticky Session이나 중앙 Redis 세션 클러스터링이 강제됨)
        assertThat(serverNodeB.get(sessionId)).isNull();

        // 2. JWT 방식: Node A에서 발급 (동일한 Secret Key 설정 공유)
        String jwtToken = jwtTokenProvider.createToken(sampleUser.getEmail(), sampleUser.getRole().name());

        // Node A 검증 -> 서명 검증 성공
        TokenUserInfo infoA = jwtTokenProvider.validateAndTokenUserInfo(jwtToken);
        assertThat(infoA.getEmail()).isEqualTo(sampleUser.getEmail());

        // Node B 검증 -> 세션 동기화나 네트워크 I/O 없이 Secret Key만으로 즉시 서명 검증 성공!
        // (수십 대의 서버로 오토스케일링되어도 완벽한 무상태 수평 확장이 가능)
        TokenUserInfo infoB = jwtTokenProvider.validateAndTokenUserInfo(jwtToken);
        assertThat(infoB.getEmail()).isEqualTo(sampleUser.getEmail());

        System.out.println("\n==========================================================================================");
        System.out.println(" 🌐 [다중 서버 확장성(Scale-out) 특성 비교]");
        System.out.println("==========================================================================================");
        System.out.println("• 세션: Node A에 생성된 세션은 Node B에서 미인지 -> Sticky Session 또는 분산 세션 저장소(Redis) 필수");
        System.out.println("• JWT : Node A에서 생성된 토큰은 Node B에서도 자체 SecretKey로 즉시 검증 성공 (완전한 Stateless 수평 확장)");
        System.out.println("==========================================================================================\n");
    }

    @Test
    @DisplayName("[5] 즉시 무효화(Instant Invalidation) 제어력: 세션(즉시 삭제 가능) vs JWT(블랙리스트 필요)")
    void compareInstantInvalidation() {
        // 1. Session 방식: 강제 로그아웃 또는 세션 만료 제어
        String sessionId = "SESSION_" + UUID.randomUUID();
        inMemorySessionStore.put(sessionId, sampleUser);
        assertThat(inMemorySessionStore.get(sessionId)).isNotNull();

        // 로그아웃 시 서버 저장소에서 삭제
        inMemorySessionStore.remove(sessionId);
        // 즉시 세션 무효화 완료 (이후 요청 차단)
        assertThat(inMemorySessionStore.get(sessionId)).isNull();

        // 2. JWT 방식: 서버에 상태가 없으므로 발급된 토큰 자체는 만료시간 전까지 유효함
        String jwt = jwtTokenProvider.createToken(sampleUser.getEmail(), sampleUser.getRole().name());
        TokenUserInfo infoBefore = jwtTokenProvider.validateAndTokenUserInfo(jwt);
        assertThat(infoBefore).isNotNull();

        // 무상태 JWT는 토큰 자체를 서버가 즉시 무효화할 수 없음 (서명 및 유효기간이 유효하므로 검증 통과)
        // 따라서 Paw-Mate는 Redis Blacklist + 짧은 Access Token 유효시간(1시간)으로 보안 한계를 보완!
        Map<String, Boolean> redisBlacklist = new ConcurrentHashMap<>();
        redisBlacklist.put(jwt, true); // 로그아웃 시 Redis에 Access Token 블랙리스트 등록

        boolean isBlacklisted = redisBlacklist.getOrDefault(jwt, false);
        assertThat(isBlacklisted).isTrue();

        System.out.println("\n==========================================================================================");
        System.out.println(" 🔐 [즉시 무효화(Invalidation) 제어력 비교]");
        System.out.println("==========================================================================================");
        System.out.println("• 세션: 서버 저장소에서 key 삭제 즉시 무효화 완벽 제어 가능");
        System.out.println("• JWT : Stateless 특성상 발급 후 토큰 자체 무효화 불가 -> Redis Blacklist & 짧은 TTL(1시간)로 해결");
        System.out.println("==========================================================================================\n");
    }

    @Test
    @DisplayName("[6] 위변조(Tampering) 방어 검증: 세션 하이재킹 vs JWT 암호화 서명 검증")
    void compareTamperingDetection() {
        // 1. JWT: 공격자가 Payload의 Role을 'USER'에서 'ADMIN'으로 임의 변조 시도
        String validJwt = jwtTokenProvider.createToken("attacker@adoptmate.com", Role.USER.name());
        String[] parts = validJwt.split("\\.");
        // parts[0]: Header, parts[1]: Payload, parts[2]: Signature

        // Base64Url 디코딩 후 Payload 권한 위조
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        String tamperedPayloadJson = payloadJson.replace("\"role\":\"USER\"", "\"role\":\"ADMIN\"");
        String tamperedPayloadBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(tamperedPayloadJson.getBytes());

        // Header와 변조된 Payload, 기존 서명을 조합한 위조 토큰 생성
        String tamperedJwt = parts[0] + "." + tamperedPayloadBase64 + "." + parts[2];

        // SecretKey 서명 검증 시 즉시 SignatureException 발생하며 차단!
        assertThatThrownBy(() -> jwtTokenProvider.validateAndTokenUserInfo(tamperedJwt))
                .isInstanceOf(SignatureException.class);

        // 2. Session: Session ID(UUID)는 단순 무작위 난수 키이므로
        // 만약 네트워크 도청/탈취(Session Hijacking)가 발생하면 세션 ID 자체로는 위조 여부를 검증할 수 없음
        String validSessionId = sampleSessionId;
        TokenUserInfo authenticated = inMemorySessionStore.get(validSessionId);
        assertThat(authenticated).isNotNull();

        System.out.println("\n==========================================================================================");
        System.out.println(" 🛡️ [위변조(Tampering) 방어 검증]");
        System.out.println("==========================================================================================");
        System.out.println("• 세션: 세션 ID 탈취 시 키 자체에 암호학적 서명이 없어 저장소 일치 시 통과 (세션 하이재킹 취약)");
        System.out.println("• JWT : 페이로드 1바이트만 변조되어도 SecretKey 기반 HMAC 서명 검증에서 즉시 예외 발생 차단");
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
