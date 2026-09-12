# PawMate Backend — 코드 및 설계 문서

이 문서는 현재 구현을 기준으로 한 내부 설계 안내서다. HTTP 경로와 요청·응답 예시는 [README](../README.md)를 기준으로 하고, 여기서는 코드의 책임, 데이터 흐름, 트랜잭션, 동시성 및 운영 원칙을 설명한다.

## 1. 시스템 개요

PawMate는 보호 동물, 입양 신청, 커뮤니티 게시글·댓글, 회원 인증을 제공하는 Spring Boot 애플리케이션이다.

```text
Client
  │ HTTP / Bearer JWT / OAuth2
  ▼
Controller ──► Service / Facade ──► Repository ──► MySQL
                     │                 
                     ├── Redis (토큰, 인증 코드, 캐시, 분산 락)
                     ├── Kakao OAuth2
                     └── SMTP mail
```

주요 기술은 Java 17, Spring Boot, Spring Data JPA/Hibernate, MySQL 8, Redis/Redisson, Spring Security, JWT다. 테스트는 H2와 Mockito를 사용한다.

## 2. 계층 규칙

| 계층 | 책임 | 금지/주의 사항 |
| --- | --- | --- |
| Controller | HTTP 파라미터 검증, 인증 주체 전달, `CommonResponse` 반환 | 비즈니스 규칙·JPA 직접 접근 금지 |
| Service | 도메인 규칙, 트랜잭션, DTO 변환 | 외부 네트워크 작업을 긴 DB 트랜잭션에 넣지 않음 |
| Facade | 분산 락처럼 서비스 호출 전후를 감싸는 조율 | DB 업무 규칙을 중복하지 않음 |
| Repository | 조회/저장과 집계 쿼리 | 컨트롤러에서 직접 사용하지 않음 |
| Domain | 상태 변경과 권한 검증 | setter 대신 의도가 드러나는 행위 메서드 사용 |
| DTO | API 입력·출력 경계 | Entity를 API 응답으로 직접 노출하지 않음 |

각 도메인의 `*ControllerDocs` 인터페이스는 Swagger/OpenAPI 설명을 컨트롤러 구현과 분리한다.

## 3. 패키지와 도메인 책임

| 패키지 | 핵심 구성 | 책임 |
| --- | --- | --- |
| `member` | `MemberService`, `MemberFacade`, `Member` | 회원가입, 로그인, 토큰 재발급, 회원 삭제, 비밀번호 변경 |
| `auth` | JWT 필터/제공자, OAuth2 서비스/핸들러 | JWT 검증, SecurityContext 구성, OAuth2 회원 연결 |
| `animal` | `AnimalService`, `AnimalFavoriteService` | 동물 CRUD, 종별·커서 조회, 찜하기 |
| `adoption` | `AdoptionFacade`, `AdoptionService`, `Adoption` | 입양 신청 및 승인/반려 상태 전이 |
| `post` | `PostService`, 게시글·좋아요·북마크 Repository | 게시글 CRUD, 검색, 정렬, 커서 목록 |
| `comment` | `CommentService`, `Comment` | 댓글/대댓글 CRUD, 최상위 댓글 페이지 조회 |
| `common` | 응답·예외·메일·분산 락 | 공통 API 형식, 오류 처리, 이메일 인증, 락 템플릿 |
| `config` | Security/Redis/JPA/CORS/Swagger | 인프라 및 프레임워크 설정 |

## 4. 데이터 모델

모든 주요 Entity는 `BaseTimeEntity`를 상속해 `createdAt`, `updatedAt`을 가진다. `Member`, `Animal`, `Adoption`, `Post`, `Comment`는 `@SQLDelete`와 `@SQLRestriction`으로 soft delete를 적용한다.

```text
Member 1 ── * Animal
Member 1 ── * Post 1 ── * Comment
Member 1 ── * Adoption * ── 1 Animal
Member * ── * Animal (AnimalFavorite)
Member * ── * Post   (PostLike, PostBookmark)
Comment 1 ── * Comment (parent/children)
```

`Animal`, `Adoption`, `Post`의 `@Version`은 동시에 수정한 요청을 감지하는 낙관적 락이다. 충돌은 전역 예외 처리에서 `409 Conflict`로 변환한다.

## 5. 인증과 보안

### JWT 인증

1. 로그인 또는 OAuth2 성공 시 Access Token과 Refresh Token을 생성한다.
2. Refresh Token은 `refreshToken:{email}` 키로 Redis에 저장한다.
3. `JwtAuthFilter`가 Bearer Access Token을 검증하고 Redis 블랙리스트를 확인한다.
4. 유효하면 `CustomUserDetails`를 SecurityContext에 넣는다.
5. 로그아웃은 Refresh Token을 삭제하고 Access Token의 남은 만료 시간만큼 블랙리스트 키를 유지한다.

회원 삭제는 DB 커밋 후 `MemberSessionInvalidationEvent`를 발행한다. `MemberRedisEventListener`가 Refresh Token 삭제와 Access Token 블랙리스트 등록을 수행하므로, DB 삭제가 롤백된 경우 세션만 먼저 무효화되지 않는다.

### OAuth2

`CustomOAuth2UserService`는 먼저 공급자 API에서 사용자 정보를 조회한다. 외부 HTTP 호출에는 DB 트랜잭션을 열지 않는다. 이어서 `OAuth2MemberPersistenceService`가 짧은 트랜잭션 안에서 소셜 ID/이메일 기준 회원을 조회·생성·연결한다. 성공 시 `OAuth2SuccessHandler`가 토큰을 발급하고 팝업용 HTML의 `postMessage`를 반환한다.

### 이메일 인증과 재설정

이메일 인증 코드, 시도 횟수, 차단 상태, 회원가입/비밀번호 재설정 완료 상태는 Redis TTL 키로 관리한다. 메일 발송은 SMTP 작업이며 DB 트랜잭션과 결합하지 않는다. 비밀번호 변경과 재설정은 BCrypt 해시를 사용한다.

## 6. 트랜잭션 원칙

### 기본 원칙

- 조회는 `@Transactional(readOnly = true)`를 사용한다.
- Entity 상태 변경은 `@Transactional` 안에서 수행한다.
- DB를 쓰지 않는 Redis/JWT 작업은 `Propagation.NOT_SUPPORTED`로 DB 커넥션을 보유하지 않는다.
- 동일 클래스 내부 호출은 Spring 프록시를 거치지 않으므로, 전파 옵션을 기대하는 메서드는 별도 Bean으로 분리하거나 외부 진입점으로 둔다.

`MemberService`는 클래스 수준에서 read-only를 기본값으로 둔다. 로그인, 로그아웃, Refresh Token 저장·재발급은 `NOT_SUPPORTED`로 예외 처리해 BCrypt/JWT/Redis 작업 중 DB 커넥션을 잡지 않는다.

### 적용된 경계

| 흐름 | 경계 |
| --- | --- |
| 로그인·토큰 재발급 | 비트랜잭션, Repository 조회만 짧은 내부 읽기 트랜잭션 |
| OAuth2 공급자 조회 | 비트랜잭션 |
| OAuth2 회원 저장 | `OAuth2MemberPersistenceService`의 짧은 쓰기 트랜잭션 |
| 입양 신청/상태 변경 | 분산 락 획득 후 짧은 쓰기 트랜잭션 |
| 동물 찜 추가/해제 | 분산 락 획득 후 `TransactionTemplate` 트랜잭션 |
| 게시글·댓글 CRUD | 서비스 단위 쓰기/읽기 트랜잭션 |

명시적으로 `Isolation`을 지정하지 않은 JPA 트랜잭션은 데이터베이스 기본 격리 수준을 따른다. MySQL 운영 기본값은 일반적으로 `REPEATABLE_READ`다. 동시 수정이 중요한 동물·입양·게시글은 `@Version`과 도메인별 Redisson 락을 함께 사용한다.

## 7. 동시성 설계

### 입양

`AdoptionFacade`는 `animal:{animalId}` Redisson 락을 먼저 획득한 뒤 `AdoptionService`를 호출한다. 따라서 같은 동물에 대한 입양 신청과 승인/반려 변경은 직렬화된다.

- 신청 시 동물은 `PROTECTED` 또는 `WAITING` 상태여야 한다.
- 신청 저장 후 동물 상태는 `WAITING`으로 바뀐다.
- 승인 시 대상 동물은 `ADOPTED`가 되고, 나머지 `PENDING` 신청은 `REJECTED`가 된다.
- 반려 후 다른 대기 신청이 없으면 동물은 다시 `PROTECTED`가 된다.

### 찜하기

`AnimalFavoriteService`의 추가/토글/해제는 `favorite:{memberId}:{animalId}` 락을 동일하게 사용한다. 락을 기다리는 동안 DB 트랜잭션이 시작되지 않도록 `NOT_SUPPORTED`와 `TransactionTemplate`을 함께 사용한다.

### 게시글 좋아요·북마크

`PostLike`와 `PostBookmark`는 `(post_id, member_id)` 유니크 제약으로 중복을 막는다. 서비스는 먼저 존재 여부를 확인해 멱등적으로 동작한다. 경쟁 요청에 대한 최종 중복 방어는 DB 유니크 제약이 담당한다.

## 8. 게시글 조회와 성능

### 커서 페이지

게시글은 offset 방식의 `Page`와 무한 스크롤용 `Slice`를 제공한다. 커서 정렬은 다음과 같다.

| sort | 정렬 | 커서 비교 값 |
| --- | --- | --- |
| `latest` | `createdAt DESC, id DESC` | 작성 시각, ID |
| `popular` | `likeCount DESC, id DESC` | 좋아요 수, ID |
| `comments` | `commentCount DESC, id DESC` | 댓글 수, ID |

커서 게시글의 작성 시각·좋아요 수·댓글 수는 서비스에서 한 번 얻어 Repository 파라미터로 전달한다. 복잡한 중첩 JPQL 서브쿼리를 피하여 Hibernate 쿼리 파서의 메모리 과다 사용을 막는다.

### 목록 응답 배치 집계

`PostService.toResponses`는 게시글 목록의 좋아요 수와 댓글 수를 `IN (...) GROUP BY` 집계 쿼리로 각각 한 번에 가져온다. 로그인 사용자는 좋아요·북마크한 게시글 ID도 각각 한 번에 조회한다. 따라서 게시글마다 네 개의 추가 쿼리를 실행하던 N+1 패턴을 피한다.

단건 상세/생성 응답은 단건 카운트 조회를 사용한다. 대량 목록의 성능 문제가 재발하면 단건 응답도 전용 projection으로 통합할 수 있다.

### OSIV

운영 프로필은 `spring.jpa.open-in-view: false`다. 엔티티의 lazy 연관관계는 서비스 트랜잭션 안에서 DTO로 변환해야 하며, 컨트롤러/직렬화 시점의 숨은 DB 접근을 허용하지 않는다.

## 9. 댓글 설계

댓글은 `parent` 자기 참조 관계로 대댓글을 표현한다. 목록 페이지는 최상위 댓글만 대상으로 하며, 각 최상위 댓글의 `children`을 함께 반환한다. 그 결과 하나의 대화 스레드가 서로 다른 페이지에 나뉘지 않는다.

댓글 생성 시 부모 댓글이 같은 게시글에 속하는지 검사한다. 수정과 삭제는 `validateAuthorOrAdmin`으로 작성자 또는 관리자만 허용한다.

## 10. 예외와 API 응답

성공 응답은 `CommonResponse`, 오류 응답은 `ApiErrorResponse` 형식이다. `GlobalExceptionHandler`가 `CustomException`, Bean Validation 오류, 낙관적 락 충돌 등을 HTTP 상태와 업무 코드로 변환한다.

새 기능은 다음 순서를 따른다.

1. `ErrorCode`와 성공 코드가 필요한지 결정한다.
2. Request/Response DTO를 작성한다.
3. Domain 행위 메서드에 상태 전이 규칙을 둔다.
4. Service에 트랜잭션과 권한 검증을 둔다.
5. Controller와 `*ControllerDocs`를 함께 작성한다.
6. 단위 테스트와 Repository 테스트를 추가한다.

## 11. 설정과 운영

| 프로필 | DB 스키마 | 로그/특징 |
| --- | --- | --- |
| `local` | 기본 `update` | SQL DEBUG, OSIV 비활성화 |
| `prod` | 환경 변수 기본 `create` | 운영에서는 반드시 `validate` 또는 마이그레이션 사용 권장, OSIV 비활성화 |
| `test` | H2 | 단위/Repository 테스트 |

운영 HikariCP 설정은 환경 변수로 풀 크기를 조절할 수 있다. 누수 감지 기준은 기본 10초이며 `HIKARI_LEAK_DETECTION_THRESHOLD`로 조정한다. 이 값은 누수 진단용이며, 느린 BCrypt·외부 HTTP·Redis 작업을 DB 트랜잭션 밖으로 분리하는 것이 우선이다.

Redis는 토큰, 이메일 인증, Spring Cache, Redisson 락에 공용으로 사용한다. Redis 장애 시 인증·락·캐시 영향 범위를 고려해 모니터링해야 한다.

## 12. 테스트 전략

| 테스트 | 목적 |
| --- | --- |
| Domain 테스트 | 상태 전이와 권한 검증 |
| Service Mockito 테스트 | 업무 규칙, 예외, Repository 협력 |
| `@DataJpaTest` | JPQL, soft delete, EntityGraph, 커서 조회 |
| Integration/benchmark 태그 | Redis 또는 동시성 환경이 필요한 검증 |

실행 명령은 다음과 같다.

```bash
./gradlew test
./gradlew integrationTest
./gradlew benchmarkTest
```

## 13. 유지보수 체크리스트

- 외부 HTTP/SMTP/Redis 대기 작업을 DB 쓰기 트랜잭션에 넣지 않는다.
- 목록 DTO 변환에서 항목별 Repository 호출이 생기면 배치 집계 또는 projection을 검토한다.
- 분산 락은 트랜잭션보다 먼저 획득하고, 해제는 반드시 `finally`에서 한다.
- 새 soft delete Entity는 `@SQLDelete`, `@SQLRestriction`, 필요한 인덱스를 함께 검토한다.
- 상태 변경 Entity는 `@Version`과 충돌 응답을 검토한다.
- 운영 DB에 `ddl-auto=create`를 사용하지 않는다.
- 보안 응답 DTO에 비밀번호·해시·내부 토큰을 포함하지 않는다.
