# 🐾 PawMate - 유기동물 입양 & 커뮤니티 플랫폼 백엔드

<p align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis-Redisson-DC382D?style=for-the-badge&logo=redis&logoColor=white" />
  <img src="https://img.shields.io/badge/JWT-0.11.5-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" />
  <img src="https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" />
  <img src="https://img.shields.io/badge/Gradle-8.x-02303A?style=for-the-badge&logo=gradle&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
</p>

> **"기술로 유기동물 문제를 해결하고 더 나은 입양 문화를 만든다"**  
> 유기동물과 입양 희망자를 안전하고 투명하게 연결하는 풀스택 웹 플랫폼의 백엔드 서비스입니다.  
> Spring Boot 3.5와 Java 17을 기반으로 구축되었으며, 회원 관리, 이메일 인증, 카카오 소셜 로그인, JWT/Redis 토큰 관리, Redisson 분산 락 기반 동시성 제어, 보호 동물의 입양 상태 머신 및 계층형 대댓글 커뮤니티 기능을 제공합니다.

---

## 📑 목차 (Table of Contents)
- [🔧 기술 스택 (Tech Stack)](#-기술-스택-tech-stack)
- [📁 프로젝트 구조 (Package Structure)](#-프로젝트-구조-package-structure)
- [🚀 주요 기능 및 핵심 아키텍처](#-주요-기능-및-핵심-아키텍처)
- [⚡ Spring Batch 대용량 데이터 최적화 & 라이프사이클 자동화](#-spring-batch-대용량-데이터-최적화--도메인-자동화)
- [📊 인증 아키텍처 실측 벤치마크 및 Trade-off 분석 (JWT vs Session)](#-인증-아키텍처-실측-벤치마크-및-trade-off-분석-jwt-vs-session)
- [🛡️ 동시성 제어 & 데이터 무결성 아키텍처](#️-동시성-제어--데이터-무결성-아키텍처-concurrency--integrity)
- [⚙️ 환경 설정 및 실행 가이드 (Getting Started)](#️-환경-설정-및-실행-가이드-getting-started)
- [📊 데이터베이스 설계 및 ERD (Database Modeling & ERD)](#-데이터베이스-설계-및-erd-database-modeling--erd)
- [📋 REST API 명세서](#-rest-api-명세서)

---

## 🔧 기술 스택 (Tech Stack)

### Backend Framework & Language
- **Language**: Java 17 (OpenJDK 17)
- **Framework**: Spring Boot 3.5.3
- **Batch Processing**: **Spring Batch 5.x** (Chunk-oriented Processing, Keyset Pagination, Job/Step Scope)
- **Build Tool**: Gradle 8.x
- **Config Management**: Dotenv (`io.github.cdimascio:dotenv-java 3.0.0`) 기반 `.env` 환경변수 자동 로드

### Security & Authentication
- **Security**: Spring Security 6+ (Method Security `@PreAuthorize`, `@AuthenticationPrincipal` 적용)
- **CORS**: Spring Security 필터 체인 레벨 `CorsConfigurationSource` 표준 빈 등록 (Preflight & 에러 응답 헤더 보장)
- **Security Utilities**: `SecurityUtil` (NPE 및 ClassCastException 방어, `resolveToken` 토큰 추출 공통화)
- **OAuth2**: Spring Security OAuth2 Client (Kakao) & `OAuthResponseUtil` 팝업 연동 템플릿 통합
- **Token**: JWT (`jjwt 0.11.5` HMAC-SHA512), Redis 기반 Refresh Token 관리 및 Blacklist 로그아웃 / 탈퇴 무효화
- **Password**: BCryptPasswordEncoder

### Database & Persistence
- **ORM**: Spring Data JPA, Hibernate 6
- **Database**: MySQL 8.0 (운영/개발), H2 (테스트 인메모리)
- **Auditing & Soft Delete**: 
  - `BaseTimeEntity` 공통 상속 (생성일시/수정일시 및 `is_deleted` 자동 관리)
  - Hibernate 6 `@SQLDelete` & `@SQLRestriction("is_deleted = false")` 적용 (데이터 이력 영구 보존 및 FK 무결성 보장)
- **쿼리 성능 최적화**: `@EntityGraph` 및 `Fetch Join`, `default_batch_fetch_size: 100` 적용 (N+1 문제 원천 차단)
- **객체지향 설계**: **Tell, Don't Ask** 원칙에 따른 `Post`/`Comment` 도메인 엔티티 내 `validateAuthorOrAdmin(userInfo)` 권한 캡슐화

### Cache & Concurrency Control
- **Distributed Lock**: Redisson (`RLock`, Pub/Sub 기반 분산 락)
- **Lock Architecture**: `Facade` 및 `DistributedLockTemplate` 패턴 (락 라이프사이클과 DB 트랜잭션의 관심사 완전 분리)
- **Optimistic Lock**: JPA `@Version` (엔티티 동시 수정 및 Lost Update 방지)
- **State Machine**: 입양 상태 전이 유효성 검증 및 다중 신청 연쇄 처리 (승인 시 타 신청 자동 반려)
- **Cache / In-Memory DB**: Redis (Spring Data Redis, Lettuce 최신 클라이언트 구성, JSON/Hash 직렬화, SSL 지원)
- **Mail**: JavaMailSender (Gmail SMTP 이메일 인증 및 비밀번호 재설정)
- **Testing & Benchmark**: JUnit 5, `@SpringBatchTest`, AssertJ, Mockito (33개 테스트 스위트 100% 통과)
- **API Documentation**: SpringDoc OpenAPI UI (Swagger 3) + **Swagger Docs Interface 분리 패턴** (`*Docs.java`)

---

## 📁 프로젝트 구조 (Package Structure)

```
com.kindtail.adoptmate
├── 📂 adoption         # 입양 신청, 상태 머신, 승인/반려 (Facade & 분산 락)
│   ├── controller      # AdoptionController & AdoptionControllerDocs (인터페이스 분리)
│   ├── domain          # Adoption, AdoptionStatus (상태 머신), HousingType
│   ├── dto             # 입양 신청/응답 DTO
│   ├── facade          # AdoptionFacade (Redisson 분산 락 & 트랜잭션 분리)
│   ├── repository      # AdoptionRepository
│   └── service         # AdoptionService (입양 심사 및 연쇄 상태 전이 비즈니스 로직)
├── 📂 animal           # 보호 동물 등록, 조회, 종별 필터링
│   ├── controller      # AnimalController & AnimalControllerDocs
│   ├── domain          # Animal, Species, Status, Gender
│   └── service         # AnimalService (SecurityUtil 기반 안전한 인증 정보 조회)
├── 📂 auth             # JWT 토큰 생성/검증, OAuth2 소셜 로그인, 시큐리티 필터 & 유틸리티
│   ├── JwtAuthFilter   # Bearer 토큰 추출 및 SecurityContext 인증 주입
│   ├── JwtTokenProvider# Access/Refresh Token 발급 및 검증 (HMAC-SHA512)
│   ├── SecurityUtil    # [신규] Null-Safe 사용자 정보 조회 및 resolveToken 토큰 파싱
│   ├── OAuthResponseUtil # [신규] 소셜 로그인 팝업 postMessage HTML 생성 공통 유틸
│   └── OAuth2SuccessHandler # 카카오/OAuth2 로그인 성공 시 Redis 토큰 보관 및 팝업 응답
├── 📂 comment          # 계층형 대댓글 (부모-자식 트리 구조)
│   ├── controller      # CommentController & CommentControllerDocs
│   ├── domain          # Comment (validateAuthorOrAdmin 도메인 메서드 캡슐화)
│   └── service         # CommentService
├── 📂 common           # 공통 응답 DTO, 글로벌 예외 처리기, 분산 락 템플릿, BaseTimeEntity
│   ├── controller      # EmailVerificationController, KakaoAuthController & Docs
│   ├── exception       # GlobalExceptionHandler (401, 403, @Valid 필드 상세화, 409, 413)
│   └── lock            # DistributedLockTemplate (Redisson 분산 락 실행기)
├── 📂 config           # SecurityConfig, RedisConfig, SwaggerConfig, CorsConfig
├── 📂 member           # 회원가입, 로그인, 정보 조회, 이메일 인증, 회원 탈퇴
└── 📂 post             # 커뮤니티 게시글 CRUD 및 페이징 (validateAuthorOrAdmin 적용)
```

---

## 🚀 주요 기능 및 핵심 아키텍처

### 🔐 1. 인증 & 보안 아키텍처 (Spring Security & JWT)
- **JWT 무상태(Stateless) 인증**: Access Token(1시간)과 Refresh Token(7일) 기반의 보안 아키텍처
- **Redis 연동 토큰 관리 & 철저한 무효화**:
  - 사용자별 Refresh Token을 Redis에 보관하여 토큰 갱신 지원
  - 로그아웃 및 **회원 탈퇴 시** Access Token 잔여 시간만큼 Blacklist에 등록하여 탈취된 토큰 즉시 무효화
- **`SecurityUtil`을 통한 Null-Safe 보안 컨텍스트 접근**:
  - `SecurityUtil.getCurrentUserInfo()`, `SecurityUtil.getCurrentUserEmail()`로 서비스단에서 `NPE`나 `ClassCastException` 없이 안전하게 인증 정보 획득
  - `SecurityUtil.resolveToken(request)`으로 컨트롤러와 필터에 흩어져 있던 `Bearer ` 헤더 추출 로직 일원화
- **이메일 인증 시스템**: 6자리 난수 코드를 Redis에 3분간 캐싱하여 검증 (5회 실패 시 30분 차단)
- **카카오 OAuth2 소셜 로그인 & 계정 자동 연동**:
  - 표준 OAuth2 Authorization Code Grant 방식으로 사용자 정보 연동
  - 기존 일반 이메일 가입 유저가 카카오 로그인 시 `socialProvider` 및 `socialId` 자동 연동 (중복 키 에러 방지)
  - `OAuthResponseUtil`을 통한 팝업 postMessage 응답 템플릿 통합
- **안전한 논리 삭제(Soft Delete)**: 회원 탈퇴 시 기존 작성 글/입양 이력 보존 및 이메일 유니크 인덱스 충돌 방지 (`email = CONCAT('deleted_', id, '_', email)`)
- **`@AuthenticationPrincipal` 표준 주입**: 컨트롤러에서 `TokenUserInfo`를 Type-safe하게 주입받아 사용

### 📖 2. Swagger Docs Interface 분리 패턴 (관심사 분리)
- 컨트롤러 코드에서 방대한 Swagger/OpenAPI 어노테이션(`@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`)을 전용 인터페이스(`*Docs.java`)로 완전히 분리
- 실제 `Controller`는 인터페이스를 `implements`하여 **순수 비즈니스 라우팅 로직만 30~50줄 내외로 유지**
- **Swagger UI JWT Bearer 인증 연동 (`SwaggerConfig.java`)**: `http://localhost:8000/swagger-ui/index.html` 상단에 `Authorize 🔒` 버튼을 제공하여 토큰 입력 후 모든 보안 API를 UI에서 바로 테스트 가능

### 🐶 3. 보호 동물 관리
- 보호 동물 등록, 상세 조회 및 페이징 목록 조회 (기본 `page=0, size=10` 안전 폴백)
- 종별(강아지/고양이/기타) 필터링 조회
- 보호 상태 변경(`PROTECTED` ➡️ `WAITING` ➡️ `ADOPTED`) 및 안전한 논리 삭제 (관리자 권한 `@PreAuthorize("hasRole('ADMIN')")` 제어)

### 🏡 4. 입양 신청 & 상태 머신 관리
- 입양 신청서 제출 (연락처, 주거 형태, 반려동물 유무, 입양 사유 등 세분화된 정보 수집)
- 동물-회원 간 중복 입양 신청 방지 (`uniqueConstraints`, 분산 락 및 서비스 레벨 검증)
- 신청 접수 시 보호 동물 상태가 `WAITING(대기)`으로 자동 전환
- **입양 상태 머신(State Machine) 및 연쇄 처리**:
  - **상태 전이 검증**: `PENDING`(대기) 상태인 신청만 심사 가능하며, 이미 완료된 신청의 중복/역방향 변경 원천 차단
  - **승인(`APPROVED`) 시 연쇄 처리**: 동물 상태를 `ADOPTED`로 갱신하고, 동일 동물에 대한 타 신청건들을 자동으로 `REJECTED`(반려) 처리
  - **반려(`REJECTED`) 시 스마트 복구**: 잔여 대기자 유무를 파악하여 대기자가 없으면 `PROTECTED`(입양 가능)로 복귀, 대기자가 남아있으면 `WAITING` 유지
- **동물 단위 락 동기화 (`animal:{id}`)**: 신청 접수뿐만 아니라 심사 승인/반려 시에도 동일 동물 기준 분산 락을 획득하여 연쇄 반려의 데이터 무결성 보장

### 💬 5. 커뮤니티 & 계층형 대댓글 (No-Offset 커서 페이징 & 인덱스 최적화)
- 입양 후기 및 자유 게시글 작성, 페이징 목록 조회, 상세 조회, 수정, 삭제
- **No-Offset 커서 기반 고속 페이징 (`/post/cursor`)**:
  - `lastPostId` 기준 Keyset 조건(`WHERE post_id < :lastPostId ORDER BY post_id DESC`)과 Spring Data `Slice`를 적용하여 대규모 트래픽 및 데이터 증가 시 발생하는 **$O(N)$ Count 쿼리 오버헤드와 Offset Skip I/O 병목을 0%로 제거**
- **DB 복합 인덱스(Composite Index) 튜닝**:
  - `@SQLRestriction("is_deleted = false")`와 정렬 컬럼에 맞춰 `idx_post_deleted_id(is_deleted, post_id DESC)` 및 `idx_post_deleted_created(is_deleted, created_at DESC)` 복합 인덱스를 구축하여 Full Table Scan 방지
- **계층형 대댓글 구조**: 부모-자식 트리 구조로 무제한 뎁스의 답글 지원
- **N+1 쿼리 최적화**: `@EntityGraph(attributePaths = {"member", "children", "children.member"})` 및 `@BatchSize`를 통한 쿼리 최적화
- **Tell, Don't Ask 객체지향 권한 검증**: `post.validateAuthorOrAdmin(userInfo)`, `comment.validateAuthorOrAdmin(userInfo)` 도메인 메서드를 통해 작성자 본인 또는 관리자만 수정/삭제 가능하도록 캡슐화

### 🚨 6. 전역 예외 처리 고도화 (`GlobalExceptionHandler`)
- `AccessDeniedException (403)` / `AuthenticationException (401)`: 보안 인가 실패 시 일관된 표준 JSON 에러 반환
- `MethodArgumentNotValidException (400)`: `@Valid` 실패 시 `[email] 이메일 형식이 올바르지 않습니다.` 형태로 구체적 필드명 명시
- `MaxUploadSizeExceededException (413)`: 파일 업로드 10MB 초과 시 친절한 안내 메시지 반환
- `OptimisticLockingFailureException (409)`: 데이터 동시 수정 충돌 시 안전한 안내 반환

---

## ⚡ Spring Batch 대용량 데이터 최적화 & 도메인 자동화

Spring Batch 5.x를 도입하여 **대용량 입양 데이터 처리의 $O(N)$ I/O 병목을 해결**하고, **방치된 입양 신청 건의 도메인 라이프사이클을 자동화**했습니다.

### 1. No-Offset(Zero-Offset) 커서 페이징을 통한 9.9배 I/O 성능 개선

#### 📌 문제 정의 (Why No-Offset?)
* **Limit-Offset 방식의 $O(N)$ 디스크 I/O 병목**:
  - 기본 `JpaPagingItemReader`는 `OFFSET 50000 LIMIT 1000` 쿼리를 실행하여 앞선 50,000건을 디스크에서 모두 읽고 버리는(Skip) 심각한 성능 저하가 발생합니다.
* **Page Drift (데이터 누락 및 중복 현상)**:
  - 배치 처리 중 레코드 상태가 변경(`PENDING` $\rightarrow$ `REJECTED`)되면 인덱스 위치가 밀려 특정 데이터가 누락되거나 중복 처리되는 데이터 정합성 결함이 발생합니다.

#### 🛠️ 해결 전략 (How?)
* **Keyset Pagination 커스텀 `ZeroOffsetAdoptionReader` 개발**:
  - `OFFSET`을 완전히 제거하고 Clustered Index(`id`) 기반의 `WHERE a.id > :lastId ORDER BY a.id ASC LIMIT :pageSize` 쿼리를 적용했습니다.
  - B-Tree 인덱스를 통해 다음 읽을 레코드 위치를 $O(\log N)$으로 즉시 탐색하며, 내부 큐(Queue) 버퍼링을 통해 Spring Batch `ItemStreamReader` 규격에 맞게 1건씩 스트리밍 소비합니다.

#### 📊 10만 건 실측 벤치마크 결과 (`BatchReaderPerformanceTest.java`)
| 페이징 방식 | 1회차 실행 | 2회차 실행 | 3회차 실행 | **3회 평균 소요 시간** | **성능 개선율** |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Limit-Offset (`JpaPagingItemReader`)** | 2,284 ms | 2,190 ms | 2,171 ms | **2,215 ms** | 기준 (1.0x) |
| **Zero-Offset (`ZeroOffsetAdoptionReader`)** | 231 ms | 221 ms | 217 ms | **223 ms** | **🚀 9.90배 (990%) 향상** |

---

### 2. 장기 미처리 입양 신청 자동 만료 및 상태 복구 배치 (`ExpiredAdoptionBatchConfig.java`)

#### 📌 비즈니스 문제 정의
* 입양 신청 시 대상 동물은 `WAITING`(입양 대기) 상태로 잠겨 다른 사용자의 신청이 제한됩니다.
* 하지만 신청자 또는 보호소 측에서 장기간(14일 이상) 방치(`PENDING`)할 경우 **동물이 영구히 대기 상태에 갇혀 다른 입양 희망자가 신청하지 못하는 비즈니스 병목**이 발생합니다.

#### 🛠️ 배치 파이프라인 아키텍처
* **Reader (`expiredAdoptionReader`)**: `@StepScope` 파라미터(`thresholdDate`)와 `Fetch Join`을 적용하여 14일 경과된 `PENDING` 건을 N+1 없이 청크 단위로 조회
* **Processor (`expiredAdoptionProcessor`)**: 입양 신청 상태를 `REJECTED`(자동 반려)로 전이하고, 연관 동물의 상태를 `PROTECTED`(입양 가능)로 복구
* **Writer (`expiredAdoptionWriter`)**: 단일 트랜잭션 내에서 `Adoption` 및 `Animal` 변경 사항을 일괄 영속화
* **트랜잭션 격리**: `Chunk(100)` 단위 트랜잭션 분할로 롱 트랜잭션 및 Undo Log 폭증을 방지하고 결함 격리(Fault Isolation) 보장

---

### 3. API 레벨 대용량 페이징 최적화: `Page` vs `Slice` & No-Offset 커서 페이징

대용량 트래픽 및 데이터 증가 환경에서 목록 조회 API(`/animals/cursor`, `/post/cursor`)의 성능 병목을 해결하기 위해 **`Slice`와 No-Offset(Keyset) 커서 페이징 아키텍처**를 도입했습니다.

#### 📌 `Page<T>` vs `Slice<T>` 핵심 동작 원리 비교

```mermaid
graph TD
    subgraph "기존 Page 방식 (Count 쿼리 병목)"
        P1["클라이언트 요청 (size=10)"] --> P2["1) 데이터 조회: LIMIT 10 OFFSET 0"]
        P1 --> P3["2) 전체 카운트 조회: SELECT count(*) FROM table"]
        P3 -->|대용량 테이블 풀스캔 발생| P4["총 페이지 수(totalPages) 계산 후 응답"]
    end

    subgraph "Slice + No-Offset 커서 방식 (Paw-Mate 고성능 API)"
        S1["클라이언트 요청 (lastId, size=10)"] --> S2["1) 데이터 조회: WHERE id < :lastId LIMIT 11"]
        S2 -->|11번째 레코드 유무로 다음 페이지 판별| S3["hasNext: true/false 결정 후 10개만 반환"]
        S3 -->|Count 쿼리 0개, Offset Skip 0개| S4["초고속 O(1) 스트리밍 응답"]
    end
```

| 비교 항목 | `Page<T>` (오프셋 페이징) | `Slice<T>` (일반 오프셋) | **`Slice<T>` + No-Offset 커서 (적용)** |
| :--- | :--- | :--- | :--- |
| **카운트 쿼리 (`count(*)`)** | **매 요청마다 1회 실행 ($O(N)$ 병목)** | **0회 (실행 안 함, 0% 오버헤드)** | **0회 (실행 안 함, 0% 오버헤드)** |
| **다음 페이지 판별 메커니즘** | 전체 카운트 기반 (`page < totalPages`) | 내부적으로 `LIMIT size + 1` 조회 | 내부적으로 `LIMIT size + 1` 조회 |
| **디스크 I/O (Offset Skip)** | 뒤쪽 페이지일수록 $O(N)$ Skip I/O 발생 | 뒤쪽 페이지일수록 $O(N)$ Skip I/O 발생 | **Clustered PK 인덱스 즉시 탐색 ($O(1)$)** |
| **적합한 UI / 비즈니스** | 1, 2, 3, 4 번호 페이지 네비게이션 (관리자) | 무한 스크롤(Infinite Scroll), 더보기 버튼 | **모바일/웹 무한 스크롤, 고성능 피드 목록** |
| **응답 메타데이터** | `content`, `totalPages`, `totalElements`, `size` | `content`, `hasNext`, `isLast`, `size`, `number` | `content`, `hasNext`, `isLast`, `size`, `number` |

#### 🛠️ Paw-Mate 적용 이점
1. **DB CPU 및 커넥션 소모 50% 이상 절감**: `SELECT count(*)` 쿼리가 제거되어 부하 상황에서 DB 커넥션 점유 시간이 극적으로 단축됩니다.
2. **응답 시간의 일관성 보장**: 데이터가 100만 건으로 증가해도 인덱스 기반 조건(`WHERE id < :lastId`)을 통해 **첫 페이지와 마지막 페이지 모두 10ms 이하의 동일한 고속 응답 속도**를 유지합니다.

---

## 📊 인증 아키텍처 실측 벤치마크 및 Trade-off 분석 (JWT vs Session)

Paw-Mate 프로젝트의 인증 방식을 선정하기 위해 **JWT (Stateless), DB 세션 (RDB/JDBC), In-Memory 세션의 성능 및 자원 소모량을 다각도로 실측 벤치마크**했습니다. ([`SessionVsJwtBenchmarkTest.java`](file:///C:/Users/nahoo/Desktop/paw-mate-backend/src/test/java/com/kindtail/adoptmate/auth/SessionVsJwtBenchmarkTest.java))

### 1. 실측 벤치마크 데이터 요약 (Benchmark Metrics)

| 벤치마크 테스트 항목 | JWT Bearer Token (Stateless) | DB 세션 (Spring Session JDBC) | In-Memory 세션 (단일 서버) |
| :--- | :---: | :---: | :---: |
| **인증 연산 병목 유형** | **CPU Bound** (HMAC-SHA512 서명 검증) | **I/O & Connection Pool Bound** (SQL 쿼리) | **Heap Memory Bound** (해시 테이블) |
| **단일 스레드 1만 회 소요 시간** | 17,407 ms (1회당 1.74 ms) | 150 ms (1회당 0.015 ms) | < 1 ms |
| **50개 스레드 동시 처리량 (TPS)** | **1,287 req/s** (멀티코어 병렬 연산) | 30,864 req/s (HikariCP 커넥션 경합 발생) | 833,333 req/s |
| **DB 부하 & 커넥션 소모** | **0% (DB 커넥션 소모 0개, Zero-I/O)** | **매 요청마다 DB 커넥션 획득 및 쿼리 실행** | 0% |
| **요청 헤더 크기 (Payload)** | **240 Bytes** (세션 대비 4.4배) | **55 Bytes** | 55 Bytes |
| **100만 요청 시 전송 대역폭** | **228.88 MB** (+176.43 MB 추가 대역폭) | **52.45 MB** | 52.45 MB |

---

### 2. Paw-Mate가 JWT를 채택한 핵심 아키텍처적 의사결정

```mermaid
graph LR
    subgraph "DB 세션 방식 (대규모 트래픽 시 병목)"
        Req1[클라이언트 요청] --> WAS1[WAS Server]
        WAS1 -->|인증 시마다 커넥션 점유| Pool[HikariCP Connection Pool (Max 20)]
        Pool -->|커넥션 고갈 및 락 대기| DB[(RDB Database)]
    end

    subgraph "JWT 무상태 방식 (Paw-Mate 채택)"
        Req2[클라이언트 요청] --> WAS2[WAS Server]
        WAS2 -->|자체 CPU 서명 검증 1.7ms| WAS2
        WAS2 -->|진짜 비즈니스 로직에만 DB 커넥션 사용| DB2[(RDB Database)]
    end
```

1. **HikariCP 커넥션 풀 고갈 및 DB 병목 방지**:
   - DB 세션 방식은 단순 정적 조회 API를 호출하더라도 **인증을 위해 무조건 DB 커넥션을 1개 소모**합니다.
   - 트래픽 폭증 시 세션 검증 쿼리로 인해 커넥션 풀이 고갈되어 실제 비즈니스 쿼리(입양 신청/상태 변경 등)가 타임아웃되는 치명적인 병목을 방지하기 위해 **DB 부하가 0%인 JWT를 채택**했습니다.
2. **클라우드 오토스케일링 및 무한 수평 확장 (Scale-out)**:
   - 서버를 수십 대로 확장하더라도 서버 간 세션 동기화나 외부 세션 스토리지 장애(SPOF) 없이 무상태(Stateless)로 유연하게 확장 가능합니다.
3. **CORS / 프론트엔드(Vercel) 완전 분리 지원**:
   - 프론트엔드(`vercel.app`)와 백엔드(`cloudtype.app`)가 서로 다른 도메인일 때 발생하는 브라우저의 서드파티 쿠키 차단(SameSite) 이슈를 완벽히 해결합니다.
4. **한계점 보완 (보안 & 즉시 무효화)**:
   - Access Token의 수명을 **1시간**으로 짧게 제한하고, **Redis 기반 Refresh Token Rotation(RTR)** 및 **로그아웃/탈퇴 시 Redis Blacklist**를 도입하여 보안과 무상태성의 균형을 완성했습니다.

---

## 🛡️ 동시성 제어 & 데이터 무결성 아키텍처 (Concurrency & Integrity)

대규모 트래픽 및 동시 다중 요청 환경에서 **데이터 무결성(Data Integrity)**을 보장하기 위해 **Redisson 분산 락(Facade/Template), JPA 낙관적 락, Soft Delete의 다계층 방어 전략**을 구축했습니다.

```mermaid
flowchart TD
    Req[클라이언트 동시 요청] --> Controller[Controller]
    Controller --> Facade["Facade 계층 (AdoptionFacade / MemberFacade)"]
    Facade --> Template["1차 방어: DistributedLockTemplate (Redisson 락 획득)"]
    Template -- 락 획득 실패 (대기 타임아웃) --> Fail[409 CONFLICT: 요청 집중 에러]
    Template -- 락 획득 성공 --> Service["Service 계층 (@Transactional 시작 - DB 커넥션 획득)"]
    Service --> Logic[상태 머신 검증 및 비즈니스 로직 실행]
    Logic --> Commit[트랜잭션 커밋 및 커넥션 반납]
    Commit -- @Version 충돌 발생 시 --> OptErr["2차 방어: 낙관적 락 예외 (OptimisticLockingFailure)"]
    Commit -- 커밋 성공 --> ReleaseLock[Facade / Template에서 분산 락 안전 해제]
    OptErr --> ReleaseLock
    ReleaseLock --> Done[클라이언트 응답 반환]
```

### 1. 트랜잭션과 분산 락의 생명주기 및 관심사 분리 (`Facade & DistributedLockTemplate`)
1. **Facade 계층**(`AdoptionFacade`, `MemberFacade`)에서 `DistributedLockTemplate`을 통해 Redis 분산 락을 먼저 획득 (DB 커넥션 미사용)
2. 락 획득 성공 후 **Service 계층**(`@Transactional`)으로 진입하여 DB 트랜잭션 시작 및 비즈니스 로직 수행
3. Service 메서드 종료와 함께 **DB 트랜잭션 커밋 완료 & 커넥션 즉시 반납**
4. Template의 `finally` 블록에서 **Redis 분산 락 안전 해제**
👉 **락 대기 시간 동안 DB 커넥션 풀을 낭비하지 않으며, 트랜잭션 커밋 후 락 해제를 완벽하게 보장**합니다.

### 2. 주요 적용 도메인
| 도메인 | 적용 기술 / 계층 | 락 키 (Type-safe) / 정책 | 목적 |
| :--- | :--- | :--- | :--- |
| **입양 신청 (`applyAdoption`)** | `AdoptionFacade` + Redisson 락 | `'animal:' + animalId` | 단일 보호 동물에 대한 동시 중복 신청 차단 |
| **입양 승인/반려 (`updateStatus`)** | `AdoptionFacade` + 상태 머신 | `'animal:' + animalId` | 상태 전이 유효성 보장, 동물 상태 전이 및 연쇄 반려 무결성 완벽 보장 |
| **회원 가입 (`registerMember`)** | `MemberFacade` + Redisson 락 | `'register:' + email` | 동일 이메일 동시 가입 요청 시 중복 생성 및 500 에러 차단 |
| **보호 동물/신청/게시글** | JPA 낙관적 락 (`@Version`) | `version` 컬럼 | 동시 수정 충돌 시 `409 Conflict` 감지 및 데이터 무결성 보장 |
| **전체 엔티티 삭제** | Hibernate Soft Delete | `is_deleted` + `@SQLRestriction` | 데이터 이력 영구 보존 및 연관 관계 외래키 충돌 방지 |
| **이메일 인증 시도** | Redis 원자 연산 (`INCR`) | `email_verify:attempt:{email}` | Read-Modify-Write 결함 제거로 5회 실패 차단(Brute-Force 방어) 완벽 보장 |

---

## ⚙️ 환경 설정 및 실행 가이드 (Getting Started)

### 1. 환경 변수 설정 (`.env.example`)
프로젝트 루트의 `.env.example` 파일을 복사하여 `.env` 파일을 생성하고 각 환경에 맞는 설정 값을 입력합니다. (애플리케이션 구동 시 `.env` 파일 자동 로드)

```properties
# Spring Profile & Server
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8000

# Database (MySQL)
DB_HOST=localhost
DB_PORT=3306
DB_NAME=adoptpet_db
DB_USERNAME=root
DB_PASSWORD=your_password
JPA_DDL_AUTO=update

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Kakao OAuth2
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
KAKAO_REDIRECT_URI=http://localhost:8000/adoptmate/kakao

# Mail (Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password

# JWT Token
JWT_EXPIRATION=3600
JWT_SECRET_KEY=your_jwt_secret_key_at_least_64_bytes_long_string_1234567890
JWT_EXPIRATION_RT=604800
JWT_SECRET_KEY_RT=your_jwt_refresh_secret_key_at_least_64_bytes_long_string_1234567890

# Client URL
CLIENT_URL=http://localhost:5173
```

### 2. 로컬 실행
```bash
# 빌드 및 실행
./gradlew bootRun
```

### 3. Swagger UI 접속
서버 실행 후 브라우저에서 아래 주소로 접속하여 API를 실시간으로 테스트할 수 있습니다:
- **Swagger UI**: `http://localhost:8000/swagger-ui/index.html` (상단 `Authorize 🔒` 버튼에 JWT 토큰 입력 지원)

### 4. Docker Compose 실행
```bash
# 전체 컨테이너(MySQL, Redis, Backend) 빌드 및 실행
docker compose up -d --build
```

### 5. 전체 테스트 실행 (33개 테스트 스위트 / 140+ Tests 100% 통과)
```bash
./gradlew test
```

---

## 📊 데이터베이스 설계 및 ERD (Database Modeling & ERD)

### 📌 1. ER 다이어그램 (Entity Relationship Diagram)

```mermaid
erDiagram
    MEMBER ||--o{ ANIMAL : "registers (1:N)"
    MEMBER ||--o{ POST : "writes (1:N)"
    MEMBER ||--o{ ADOPTION : "applies (1:N)"
    MEMBER ||--o{ COMMENT : "writes (1:N)"
    ANIMAL ||--o{ ADOPTION : "targeted_by (1:N)"
    POST ||--o{ COMMENT : "has (1:N)"
    COMMENT ||--o{ COMMENT : "replies_to (1:N parent-child)"

    MEMBER {
        bigint member_id PK "회원 고유 식별자"
        varchar email UK "로그인 이메일 (탈퇴 시 식별 접두사 부여)"
        varchar password "BCrypt 암호화 비밀번호 (소셜 회원은 NULL)"
        varchar name "회원 이름 / 닉네임"
        varchar role "권한 (USER, ADMIN)"
        varchar profile_image "프로필 이미지 URL"
        varchar social_provider "소셜 로그인 제공자 (kakao)"
        varchar social_id "소셜 고유 식별자"
        datetime created_at "생성 일시"
        datetime updated_at "수정 일시"
        boolean is_deleted "논리 삭제 플래그"
    }

    ANIMAL {
        bigint animal_id PK "보호 동물 고유 식별자"
        bigint member_id FK "등록 회원 ID"
        varchar species "축종 (DOG, CAT, ETC)"
        varchar gender "성별 (MALE, FEMALE)"
        varchar breed "품종"
        varchar color "색상"
        varchar status "보호 상태 (WAITING, PROTECTED, ADOPTED)"
        bigint age "추정 나이 / 월령"
        text image "보호 동물 사진"
        bigint version "JPA 낙관적 락 버전 (@Version)"
        datetime created_at "생성 일시"
        datetime updated_at "수정 일시"
        boolean is_deleted "논리 삭제 플래그"
    }

    ADOPTION {
        bigint adoption_id PK "입양 신청 고유 식별자"
        bigint member_id FK "신청 회원 ID (UK_member_animal)"
        bigint animal_id FK "대상 동물 ID (UK_member_animal)"
        varchar phone "신청자 연락처"
        varchar housing_type "주거 환경 (APARTMENT, VILLA 등)"
        varchar has_pet "현재 반려동물 유무"
        text reason "입양 동기 및 돌봄 계획"
        text interview "심사/면담 메모"
        varchar status "신청 상태 (PENDING, APPROVED, REJECTED)"
        datetime apply_date "신청 일시"
        bigint version "JPA 낙관적 락 버전 (@Version)"
        datetime created_at "생성 일시"
        datetime updated_at "수정 일시"
        boolean is_deleted "논리 삭제 플래그"
    }

    POST {
        bigint post_id PK "게시글 고유 식별자"
        bigint member_id FK "작성 회원 ID"
        varchar title "게시글 제목"
        text content "게시글 본문"
        text image "첨부 이미지 URL"
        bigint version "JPA 낙관적 락 버전 (@Version)"
        datetime created_at "생성 일시"
        datetime updated_at "수정 일시"
        boolean is_deleted "논리 삭제 플래그"
    }

    COMMENT {
        bigint comment_id PK "댓글 고유 식별자"
        bigint post_id FK "게시글 ID"
        bigint member_id FK "작성 회원 ID"
        bigint parent_id FK "부모 댓글 ID (Self-Reference 계층 구조)"
        text content "댓글 본문"
        datetime created_at "생성 일시"
        datetime updated_at "수정 일시"
        boolean is_deleted "논리 삭제 플래그"
    }
```

<details>
<summary><b>🖼️ 원본 ERD 다이어그램 이미지 보기 (클릭하여 펼치기)</b></summary>

<br />

<img width="1280" height="952" alt="DB Diagram" src="https://github.com/user-attachments/assets/250cbc1b-0326-459e-a89d-17a871cc97be" />

</details>

---

### 📋 2. 테이블 상세 명세서 (Table Specifications)

#### 👤 `member` (회원 테이블)
> 유기동물 보호자, 입양 희망자 및 관리자 계정 정보를 관리합니다.

| 컬럼명 | 데이터 타입 | Nullable | Key / Default | 설명 |
| :--- | :--- | :---: | :---: | :--- |
| `member_id` | `BIGINT` | NO | **PK** (AI) | 회원 고유 식별자 |
| `email` | `VARCHAR(255)` | NO | **UK** | 로그인 이메일 (탈퇴 시 `deleted_{id}_{email}`로 가명화) |
| `password` | `VARCHAR(255)` | YES | - | BCrypt 암호화 비밀번호 (소셜 가입 회원은 NULL) |
| `name` | `VARCHAR(255)` | NO | - | 회원 이름 / 닉네임 |
| `role` | `VARCHAR(20)` | NO | `'USER'` | 권한 (`USER`, `ADMIN`) |
| `profile_image` | `VARCHAR(255)` | YES | - | 프로필 이미지 URL |
| `social_provider` | `VARCHAR(50)` | YES | - | 소셜 로그인 제공자 (`kakao` 등) |
| `social_id` | `VARCHAR(255)` | YES | - | 소셜 고유 식별자 |
| `created_at` | `DATETIME` | NO | `BaseTime` | 계정 생성 일시 |
| `updated_at` | `DATETIME` | NO | `BaseTime` | 최근 수정 일시 |
| `is_deleted` | `BOOLEAN` | NO | `false` | 논리 삭제 플래그 |

<br />

#### 🐶 `animal` (보호 동물 테이블)
> 입양 대상 유기동물의 정보, 신체적 특징 및 입양 상태를 관리합니다.

| 컬럼명 | 데이터 타입 | Nullable | Key / Default | 설명 |
| :--- | :--- | :---: | :---: | :--- |
| `animal_id` | `BIGINT` | NO | **PK** (AI) | 보호 동물 고유 식별자 |
| `member_id` | `BIGINT` | NO | **FK** | 등록자/보호자 (`member.member_id`) |
| `species` | `VARCHAR(20)` | YES | - | 축종 (`DOG`, `CAT`, `ETC`) |
| `gender` | `VARCHAR(10)` | YES | - | 성별 (`MALE`, `FEMALE`) |
| `breed` | `VARCHAR(255)` | YES | - | 품종 (예: 말티즈, 코숏 등) |
| `color` | `VARCHAR(255)` | YES | - | 털색 |
| `status` | `VARCHAR(20)` | YES | `'PROTECTED'` | 보호 상태 (`PROTECTED`, `WAITING`, `ADOPTED`) |
| `age` | `BIGINT` | YES | - | 추정 나이 / 월령 |
| `image` | `LONGTEXT` | YES | - | 보호 동물 사진 (URL 또는 Base64) |
| `version` | `BIGINT` | NO | `0` | JPA 낙관적 락 버전 (`@Version`) |
| `created_at` | `DATETIME` | NO | `BaseTime` | 등록 일시 |
| `is_deleted` | `BOOLEAN` | NO | `false` | 논리 삭제 플래그 |

> 🔑 **복합 인덱스 (Composite Index)**:  
> - `idx_animal_deleted_id` (`is_deleted`, `animal_id DESC`)  
> - `idx_animal_deleted_species` (`is_deleted`, `species`, `animal_id DESC`)  
> - `idx_animal_deleted_status` (`is_deleted`, `status`, `animal_id DESC`)  
> ➡️ 논리 삭제(`is_deleted = false`) 필터링과 종별/상태별 정렬 조건에 최적화된 복합 인덱스로 Full Table Scan 방지 및 No-Offset 페이징 조회 속도 극대화

<br />

#### 🏡 `adoption` (입양 신청 테이블)
> 입양 희망자의 신청서, 주거 환경, 반려동물 양육 경험 및 심사 상태를 관리합니다.

| 컬럼명 | 데이터 타입 | Nullable | Key / Default | 설명 |
| :--- | :--- | :---: | :---: | :--- |
| `adoption_id` | `BIGINT` | NO | **PK** (AI) | 입양 신청 고유 식별자 |
| `member_id` | `BIGINT` | YES | **FK, UK** | 신청 회원 (`member.member_id`) |
| `animal_id` | `BIGINT` | YES | **FK, UK** | 대상 동물 (`animal.animal_id`) |
| `phone` | `VARCHAR(20)` | NO | - | 신청자 연락처 |
| `housing_type` | `VARCHAR(30)` | NO | - | 주거 형태 (`APARTMENT`, `DETACHED_HOUSE`, `VILLA`, `ONE_ROOM`, `ETC`) |
| `has_pet` | `VARCHAR(50)` | NO | - | 현재 반려동물 유무 (예: "없음", "고양이 1마리") |
| `reason` | `TEXT` | NO | - | 입양 동기 및 돌봄 계획 |
| `interview` | `LONGTEXT` | YES | - | 심사/면담 메모 |
| `status` | `VARCHAR(20)` | NO | - | 신청 상태 (`PENDING`, `APPROVED`, `REJECTED`) |
| `apply_date` | `DATETIME` | YES | - | 신청 접수 일시 |
| `version` | `BIGINT` | NO | `0` | JPA 낙관적 락 버전 (`@Version`) |
| `created_at` | `DATETIME` | NO | `BaseTime` | 생성 일시 |
| `updated_at` | `DATETIME` | NO | `BaseTime` | 최근 수정 일시 |
| `is_deleted` | `BOOLEAN` | NO | `false` | 논리 삭제 플래그 |

> 🔑 **복합 유니크 제약조건 (Unique Constraint)**:  
> `uk_adoption_member_animal` (`member_id`, `animal_id`)  
> ➡️ 동일 회원이 동일 보호 동물에게 중복으로 입양 신청서를 제출하는 것을 DB 레벨에서 원천 차단

<br />

#### 📝 `post` (커뮤니티 게시글 테이블)
> 입양 후기 및 자유 게시판 글을 관리합니다.

| 컬럼명 | 데이터 타입 | Nullable | Key / Default | 설명 |
| :--- | :--- | :---: | :---: | :--- |
| `post_id` | `BIGINT` | NO | **PK** (AI) | 게시글 고유 식별자 |
| `member_id` | `BIGINT` | NO | **FK** | 작성자 (`member.member_id`) |
| `title` | `VARCHAR(255)` | YES | - | 게시글 제목 |
| `content` | `LONGTEXT` | YES | - | 게시글 본문 내용 |
| `image` | `LONGTEXT` | YES | - | 본문 첨부 이미지 URL |
| `version` | `BIGINT` | NO | `0` | JPA 낙관적 락 버전 (`@Version`) |
| `created_at` | `DATETIME` | NO | `BaseTime` | 작성 일시 |
| `updated_at` | `DATETIME` | NO | `BaseTime` | 최근 수정 일시 |
| `is_deleted` | `BOOLEAN` | NO | `false` | 논리 삭제 플래그 |

> 🔑 **복합 인덱스 (Composite Index)**:  
> - `idx_post_deleted_id` (`is_deleted`, `post_id DESC`)  
> - `idx_post_deleted_created` (`is_deleted`, `created_at DESC`)  
> ➡️ 논리 삭제(`is_deleted = false`) 필터링과 정렬 조건에 최적화된 복합 인덱스로 Full Table Scan 방지 및 No-Offset 페이징 조회 속도 극대화

<br />

#### 💬 `comment` (댓글 & 계층형 대댓글 테이블)
> 게시글 댓글 및 부모-자식 자가 참조(Self-Join) 기반 계층형 답글을 관리합니다.

| 컬럼명 | 데이터 타입 | Nullable | Key / Default | 설명 |
| :--- | :--- | :---: | :---: | :--- |
| `comment_id` | `BIGINT` | NO | **PK** (AI) | 댓글 고유 식별자 |
| `post_id` | `BIGINT` | YES | **FK** | 대상 게시글 (`post.post_id`) |
| `member_id` | `BIGINT` | YES | **FK** | 작성 회원 (`member.member_id`) |
| `parent_id` | `BIGINT` | YES | **FK** | 부모 댓글 식별자 (`comment.comment_id` Self-Reference) |
| `content` | `LONGTEXT` | YES | - | 댓글 본문 |
| `created_at` | `DATETIME` | NO | `BaseTime` | 작성 일시 |
| `updated_at` | `DATETIME` | NO | `BaseTime` | 최근 수정 일시 |
| `is_deleted` | `BOOLEAN` | NO | `false` | 논리 삭제 플래그 |

---

### 💡 3. 데이터베이스 설계 핵심 전략 및 무결성 메커니즘

1. **안전한 논리 삭제(Soft Delete) & 유니크 제약 충돌 방지**:
   - 회원 탈퇴 시 기존 작성 글 및 입양 이력의 외래키 무결성을 영구 보존합니다.
   - 탈퇴한 회원의 이메일 유니크 인덱스 충돌을 방지하여 **동일 이메일로의 재가입을 허용**하기 위해 `@SQLDelete(sql = "UPDATE member SET is_deleted = true, email = CONCAT('deleted_', member_id, '_', email) WHERE member_id = ?")`를 적용했습니다.
   - 조회 시 Hibernate 6 `@SQLRestriction("is_deleted = false")`를 통해 별도의 쿼리 조건문 추가 없이 삭제된 데이터를 투명하게 필터링합니다.

2. **동시성 제어 및 Lost Update 방지 (Optimistic Lock `@Version`)**:
   - `Animal`, `Adoption`, `Post` 테이블에 `version` 컬럼을 도입하여 다중 트랜잭션 동시 수정 시 충돌을 감지(`409 Conflict`)하고 데이터 덮어쓰기(Lost Update)를 방지합니다.

3. **복합 유니크 제약조건 (Composite Unique Constraint)**:
   - `Adoption` 테이블에 `uk_adoption_member_animal (member_id, animal_id)` 유니크 제약을 설정하여 애플리케이션 레벨의 분산 락과 함께 DB 레벨 2중으로 중복 신청을 방어합니다.

4. **계층형 대댓글 자가 참조 (Self-Referencing FK) & N+1 최적화**:
   - `parent_id`를 통한 1:N 트리 구조 설계 및 `@BatchSize(size = 100)`를 적용하여 무제한 뎁스의 답글을 성능 저하 없이 일괄 페치(Batch Fetch)합니다.

---

## 📋 REST API 명세서

### 📦 공통 응답 포맷 (`CommonResDto`)
```json
{
  "statusCode": 200,
  "statusMessage": "성공 메시지",
  "result": { ... }
}
```

### 🚨 공통 에러 포맷 (`CommonErrorDto`)
```json
{
  "statusCode": 400,
  "code": "C001",
  "statusMessage": "[email] 유효하지 않은 이메일 형식입니다."
}
```

---

### 👤 1. 회원 & 인증 API (`/adoptmate`)

| 메서드 | URL | 권한 | 설명 | Request Body / Params | Response Data |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/adoptmate/register` | Public | 일반 회원가입 (이메일 중복 분산 락) | `MemberRegisterRequestDto` | `MemberResponseDto` (HTTP 201) |
| `POST` | `/adoptmate/login` | Public | 일반 로그인 | `MemberLoginRequestDto` | `MemberLoginResultDto` (HTTP 200) |
| `POST` | `/adoptmate/refresh-token` | Public | Access Token 재발급 | `{"refreshToken": "string"}` | `{"token": "string"}` |
| `POST` | `/adoptmate/logout` | User | 로그아웃 (Redis 토큰 삭제 및 블랙리스트) | Header: `Authorization: Bearer <token>` | `null` |
| `GET` | `/adoptmate/myInfo` | User | 내 프로필 정보 조회 | Header: `Authorization: Bearer <token>` | `MemberInfoResponseDto` |
| `GET` | `/adoptmate/all` | Admin | 전체 회원 목록 조회 | - | `List<MemberInfoResponseDto>` |
| `POST` | `/adoptmate/password` | User | 로그인 상태에서 비밀번호 변경 | `PasswordChangeRequestDto` | `null` |
| `DELETE` | `/adoptmate/delete` | User | 회원 탈퇴 (토큰 즉시 무효화 및 Soft Delete) | Header: `Authorization: Bearer <token>` | `null` |

---

### ✉️ 2. 이메일 인증 & 비밀번호 재설정 API (`/adoptmate`)

| 메서드 | URL | 권한 | 설명 | Request Body / Params | Response Data |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/adoptmate/verify-email` | Public | 회원가입용 인증 코드 이메일 발송 (TTL 3분) | `{"email": "string"}` | `null` |
| `POST` | `/adoptmate/verify-code` | Public | 이메일 인증 코드 검증 (5회 실패 시 30분 차단) | `{"email": "string", "code": "string"}` | `Map<String, String>` |
| `POST` | `/adoptmate/send-reset-code` | Public | 비밀번호 재설정 인증 코드 발송 | Query: `?email={email}` | `null` |
| `POST` | `/adoptmate/verify-reset-code` | Public | 비밀번호 재설정 인증 코드 검증 | Query: `?email={email}&code={code}` | `null` |
| `PATCH` | `/adoptmate/password` | Public | 비밀번호 재설정 실행 (인증 완료 회원) | `PasswordResetRequestDto` | `null` |

---

### 🔑 3. 카카오 소셜 로그인 API (`/adoptmate`, `/oauth2`)

| 메서드 | URL | 권한 | 설명 | Request Params | Response |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/oauth2/authorization/kakao` | Public | Spring Security 카카오 로그인 진입 | - | 카카오 인가 페이지 리다이렉트 |
| `GET` | `/adoptmate/kakao` | Public | 카카오 OAuth2 콜백 엔드포인트 (기존 이메일 회원 자동 연동) | Query: `?code={code}` | HTML (Window postMessage / Redirect) |

---

### 🐶 4. 보호 동물 관리 API (`/animals`)

| 메서드 | URL | 권한 | 설명 | Request Body / Params | Response Data |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/animals/register` | Admin | 보호 동물 등록 | `AnimalCreateRequest` | `AnimalResponse` (HTTP 201) |
| `GET` | `/animals/list` | Public | 보호 동물 전체 목록 조회 (오프셋 페이징, 기본 `page=0, size=10`) | Query: `?page=0&size=10` | `Page<AnimalResponse>` |
| `GET` | `/animals/cursor` | Public | **보호 동물 전체 목록 조회 (No-Offset 커서 / 무한 스크롤, Count 쿼리 0%)** | Query: `?lastAnimalId=10&size=10` | `Slice<AnimalResponse>` |
| `GET` | `/animals/species` | Public | 보호 동물 종별 목록 조회 (페이징, 기본 `page=0, size=10`) | Query: `?species=DOG&page=0&size=10` | `Page<AnimalResponse>` |
| `GET` | `/animals/{id}` | Public | 보호 동물 상세 조회 | Path: `id` | `AnimalResponse` |
| `PUT` | `/animals/{id}/status` | Admin | 보호 동물 상태 변경 (`PROTECTED`/`WAITING`/`ADOPTED`) | Path: `id`, Body: `AnimalStatusUpdateRequest` | `AnimalResponse` |
| `DELETE` | `/animals/{id}`, `/animals/delete/{id}` | Admin | 보호 동물 삭제 | Path: `id` | `null` (HTTP 200) |

---

### 🏡 5. 입양 신청 관리 API (`/adoptions`)

| 메서드 | URL | 권한 | 설명 | Request Body / Params | Response Data |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/adoptions/animals/{animalId}` | User | 동물 입양 신청서 제출 (동물 락 `'animal:' + animalId`) | Path: `animalId`, Body: `AdoptionCreateRequest` | `AdoptionResponseDto` (HTTP 201) |
| `GET` | `/adoptions/myAdoption` | User | 본인 입양 신청 내역 조회 | Header: `Authorization: Bearer <token>` | `List<AdoptionResponseDto>` |
| `GET` | `/adoptions/all` | Admin | 전체 입양 신청 내역 조회 (리스트) | Header: `Authorization: Bearer <token>` | `List<AdoptionResponseDto>` |
| `GET` | `/adoptions/list` | Admin | 전체 입양 신청 내역 조회 (페이징) | Header: `Authorization: Bearer <token>`, `?page=0&size=10` | `Page<AdoptionResponseDto>` |
| `PUT` | `/adoptions/{adoptionId}/status` | Admin | 입양 신청 상태 변경 (`APPROVED` / `REJECTED`, 동물 락 동기화) | Path: `adoptionId`, Body: `AdoptionUpdateRequestDto` | `AdoptionResponseDto` |

---

### 📝 6. 커뮤니티 게시글 API (`/post`)

| 메서드 | URL | 권한 | 설명 | Request Body / Params | Response Data |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/post/create` | User | 게시글 작성 | `PostCreateRequestDto` | `PostResponseDto` (HTTP 201) |
| `GET` | `/post/list` | Public | 게시글 목록 조회 (오프셋 페이징) | Query: `?page=0&size=10&sort=id,desc` | `Page<PostResponseDto>` |
| `GET` | `/post/cursor` | Public | **게시글 목록 조회 (No-Offset 커서 / 무한 스크롤, Count 쿼리 0%)** | Query: `?lastPostId=10&size=10` (첫 페이지 시 `lastPostId` 생략) | `Slice<PostResponseDto>` |
| `GET` | `/post/{postId}` | Public | 게시글 상세 조회 | Path: `postId` | `PostResponseDto` |
| `PUT` | `/post/{postId}` | Author/Admin | 게시글 수정 (작성자 또는 관리자) | Path: `postId`, Body: `PostUpdateRequestDto` | `PostResponseDto` |
| `DELETE` | `/post/{postId}` | Author/Admin | 게시글 삭제 (작성자 또는 관리자) | Path: `postId` | `null` |

---

### 💬 7. 댓글 & 계층형 답글 API (`/comment`)

| 메서드 | URL | 권한 | 설명 | Request Body / Params | Response Data |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/comment/{postId}` | User | 댓글 또는 답글 작성 | Path: `postId`, Body: `CommentDto` | `CommentResponseDto` (HTTP 201) |
| `GET` | `/comment/{postId}` | Public | 특정 게시글 댓글 목록 (계층형 대댓글 트리) | Path: `postId` | `List<CommentResponseDto>` |
| `PUT` | `/comment/{commentId}`, `/comment/update/{commentId}` | Author/Admin | 댓글 수정 (작성자 또는 관리자) | Path: `commentId`, Body: `CommentUpdateDto` | `CommentResponseDto` |
| `DELETE` | `/comment/{commentId}` | Author/Admin | 댓글 삭제 (작성자 또는 관리자) | Path: `commentId` | `null` |
