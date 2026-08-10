# 🐾 PawMate - 입양동물 플랫폼 백엔드

유기동물과 입양희망자를 연결하는 풀스택 웹 플랫폼의 백엔드 서비스입니다.  
Spring Boot와 Java 17을 기반으로 구축되었으며, 회원 관리, 이메일 인증, 카카오 소셜 로그인, JWT/Redis 토큰 관리, 보호 동물의 입양 신청 및 계층형 대댓글 커뮤니티 기능을 제공합니다.

---

## 🌱 프로젝트 개요

> "기술로 유기동물 문제를 해결할 수 없을까?"라는 고민에서 출발한 개인 프로젝트입니다.  
보호소에서 봉사하며 느꼈던 현실적인 불편함을 바탕으로, 입양 절차를 온라인으로 쉽게 진행하고 소통할 수 있도록 제작하였습니다.

---

## 🔧 기술 스택

### Backend Framework & Language
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.3
- **Security & Auth**: Spring Security, OAuth2 Client, JWT (jjwt 0.11.5)
- **Database / ORM**: Spring Data JPA, H2 / MySQL
- **Cache & Session**: Redis (Spring Data Redis)
- **Mail Service**: JavaMailSender (SMTP)
- **Build Tool**: Gradle

---

## 🚀 주요 기능

### 🔐 인증 및 회원 관리
- 일반 회원가입 및 로그인 (JWT Access Token & Refresh Token 기반)
- 이메일 인증 코드 발송 및 검증 (Redis 연동)
- 비밀번호 재설정 기능 (안전한 인증 확인 검증 적용)
- 카카오 OAuth2 소셜 로그인 지원
- Redis 기반 토큰 자동 갱신 및 로그아웃 블랙리스트 관리

### 🐶 보호 동물 관리
- 보호 동물 등록, 상세 조회 및 페이징 목록 조회
- 보호 동물 상태 수정 (보호중, 입양완료 등) 및 삭제 (관리자 권한)

### 🏡 입양 신청 관리
- 유기동물 입양 신청서 제출 및 중복 신청 방지
- 사용자별 내 입양 신청 내역 조회
- 관리자 전체 입양 신청 내역 조회 및 상태 변경 (승인 / 거절)

### 💬 커뮤니티 & 계층형 댓글
- 입양 후기 및 게시글 작성, 조회(페이징), 수정, 삭제
- 계층형 댓글 및 대댓글(답글) 작성/조회 (EntityGraph 페치 조인 적용으로 N+1 쿼리 최적화)

---

## 📊 시스템 구조 및 다이어그램

### DB 설계 (ERD)
<img width="1280" height="952" alt="DB Diagram" src="https://github.com/user-attachments/assets/250cbc1b-0326-459e-a89d-17a871cc97be" />

### 유스케이스 다이어그램
<img width="1104" height="930" alt="Use Case Diagram" src="https://github.com/user-attachments/assets/ee9125c5-c1a1-4dbd-a8b3-63ffeee61a5d" />

---

## 📋 REST API 명세서

### 📦 공통 응답 포맷

본 프로젝트의 API는 일관된 응답 구조(`CommonResDto`)를 사용합니다.

```json
{
  "statusCode": 200,
  "statusMessage": "성공 메시지",
  "result": { ... }
}
```

---

### 👤 1. 회원 & 인증 API (`/adoptmate`)

| 메서드 | URL | 권한 | 설명 | Request Body / Params | Response Data |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/adoptmate/register` | Public | 일반 회원가입 | `MemberRegisterRequestDto` | `MemberResponseDto` |
| `POST` | `/adoptmate/login` | Public | 일반 로그인 | `MemberLoginResponseDto` | `MemberLoginResultDto` |
| `POST` | `/adoptmate/refresh-token` | Public | Access Token 재발급 | `{"refreshToken": "string"}` | `{"token": "string"}` |
| `POST` | `/adoptmate/logout` | User | 로그아웃 (토큰 블랙리스트/Redis 파기) | Header: `Authorization: Bearer <token>` | `null` |
| `GET` | `/adoptmate/myInfo` | User | 내 정보 조회 | Header: `Authorization: Bearer <token>` | `MemberInfoRequestDto` |
| `GET` | `/adoptmate/all` | Admin | 전체 회원 목록 조회 | - | `List<MemberInfoRequestDto>` |
| `POST` | `/adoptmate/password` | User | 비밀번호 변경 (로그인 상태) | `PasswordChangeRequestDto` | `PasswordChangeRequestDto` |
| `DELETE` | `/adoptmate/delete` | User | 회원 탈퇴 | Header: `Authorization: Bearer <token>` | `TokenUserInfo` |

<details>
<summary><b>📄 회원 관련 DTO 상세</b></summary>

- **MemberRegisterRequestDto** (회원가입 요청): `name` (String), `email` (String), `password` (String), `role` (Role: `USER` \| `ADMIN`)
- **MemberLoginResponseDto** (로그인 요청 / 비밀번호 변경): `email` (String, 필수/이메일형식), `password` (String, 필수)
- **MemberLoginResultDto** (로그인 응답): `token` (String), `refreshToken` (String), `email` (String), `role` (Role)
- **MemberInfoRequestDto** (회원 정보): `id` (Long), `name` (String), `email` (String), `role` (Role)
- **MemberResponseDto** (회원 응답): `id` (Long), `name` (String), `email` (String), `password` (String), `role` (Role), `profileImage` (String), `socialProvider` (String), `socialId` (String)
- **PasswordChangeRequestDto** (비밀번호 변경 요청): `currentPassword` (String), `newPassword` (String)

</details>

---

### ✉️ 2. 이메일 인증 & 비밀번호 재설정 API (`/adoptmate`)

| 메서드 | URL | 권한 | 설명 | Request Body / Params | Response Data |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/adoptmate/verify-email` | Public | 회원가입용 이메일 인증 코드 발송 | `{"email": "string"}` | `null` |
| `POST` | `/adoptmate/verify-code` | Public | 이메일 인증 코드 검증 | `{"email": "string", "code": "string"}` | `Map<String, String>` |
| `POST` | `/adoptmate/send-reset-code` | Public | 비밀번호 재설정 인증 코드 발송 | Query: `?email={email}` | `null` |
| `POST` | `/adoptmate/verify-reset-code` | Public | 비밀번호 재설정 인증 코드 검증 | Query: `?email={email}&code={code}` | `null` |
| `PATCH` | `/adoptmate/password` | Public | 비밀번호 재설정 (인증 완료 후) | `MemberLoginResponseDto` | `null` |

---

### 🔑 3. 카카오 소셜 로그인 API (`/adoptmate`)

| 메서드 | URL | 권한 | 설명 | Request Params | Response |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/adoptmate/kakao` | Public | 카카오 OAuth2 콜백 | Query: `?code={code}` | HTML (Window postMessage / Redirect) |

---

### 🐶 4. 보호 동물 관리 API (`/animals`)

| 메서드 | URL | 권한 | 설명 | Request Body / Params | Response Data |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/animals/register` | Admin | 보호 동물 등록 | `AnimalCreateRequest` | `Animal` (Entity) |
| `GET` | `/animals/list` | Public | 보호 동물 목록 조회 (페이징) | Query: `?page=0&size=10` | `Page<AnimalResponse>` |
| `GET` | `/animals/{id}` | Public | 보호 동물 상세 조회 | Path: `id` | `AnimalResponse` |
| `PUT` | `/animals/{id}/status` | Admin | 보호 동물 상태 변경 | Path: `id`, Body: `AnimalStatusUpdateRequest` | `AnimalResponse` |
| `DELETE` | `/animals/delete/{id}` | Admin | 보호 동물 삭제 | Path: `id` | HTTP 204 No Content |

<details>
<summary><b>📄 보호 동물 관련 DTO & Enum 상세</b></summary>

- **AnimalCreateRequest**: `species` (String), `breed` (String), `color` (String), `image` (String), `age` (Long), `gender` (`Gender`), `status` (`Status`), `member` (Member)
- **AnimalResponse**: `id` (Long), `species` (String), `breed` (String), `color` (String), `status` (`Status`), `age` (Long), `gender` (`Gender`), `image` (String)
- **AnimalStatusUpdateRequest**: `status` (`Status`)
- **Enums**:
  - `Status`: `WAITING` (대기), `PROTECTED` (보호중), `ADOPTED` (입양완료)
  - `Gender`: `MALE` (수컷), `FEMALE` (암컷)

</details>

---

### 🏡 5. 입양 신청 관리 API (`/adoptions`)

| 메서드 | URL | 권한 | 설명 | Request Body / Params | Response Data |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/adoptions/animals/{animalId}` | User | 동물 입양 신청 | Path: `animalId`, Body: `AdoptionRequestDto` | `AdoptionResponseDto` |
| `GET` | `/adoptions/myAdoption` | User | 내 입양 신청 내역 조회 | Header: `Authorization: Bearer <token>` | `List<AdoptionResponseDto>` |
| `GET` | `/adoptions/all` | Admin/User | 전체 입양 신청 내역 조회 | - | `List<AdoptionResponseDto>` |
| `PUT` | `/adoptions/{adoptionId}/status` | User/Admin | 입양 신청 상태 변경 | Path: `adoptionId`, Body: `AdoptionUpdateRequestDto` | `AdoptionResponseDto` |

<details>
<summary><b>📄 입양 신청 관련 DTO & Enum 상세</b></summary>

- **AdoptionRequestDto**: `memberId` (Long), `animalId` (Long), `interview` (String - 신청 사유/인터뷰), `status` (`AdoptionStatus`)
- **AdoptionResponseDto**: `adoptionId` (Long), `memberName` (String), `status` (`AdoptionStatus`), `interviewer` (String), `animalImage` (String), `applyDate` (String)
- **AdoptionUpdateRequestDto**: `adoptionStatus` (`AdoptionStatus`)
- **Enum**:
  - `AdoptionStatus`: `PENDING` (신청대기), `APPROVED` (승인), `REJECTED` (거절)

</details>

---

### 📝 6. 커뮤니티 게시글 API (`/post`)

| 메서드 | URL | 권한 | 설명 | Request Body / Params | Response Data |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/post/create` | User | 게시글 작성 | `PostCreateRequestDto` | `Post` (Entity) |
| `GET` | `/post/list` | Public | 게시글 목록 조회 (페이징) | Query: `?page=0&size=10&sort=id,desc` | `Page<PostResponseDto>` |
| `GET` | `/post/{postId}` | Public | 게시글 상세 조회 | Path: `postId` | `PostResponseDto` |
| `PUT` | `/post/{postId}` | Author/Admin | 게시글 수정 | Path: `postId`, Body: `PostUpdateRequestDto` | `PostResponseDto` |
| `DELETE` | `/post/{postId}` | Author/Admin | 게시글 삭제 | Path: `postId` | `null` |

<details>
<summary><b>📄 게시글 관련 DTO 상세</b></summary>

- **PostCreateRequestDto**: `title` (String), `content` (String), `img` (String), `name` (String), `dateTime` (LocalDateTime)
- **PostResponseDto**: `id` (Long), `title` (String), `content` (String), `email` (String), `name` (String), `createAt` (LocalDateTime), `img` (String)
- **PostUpdateRequestDto**: `title` (String), `img` (String), `content` (String)

</details>

---

### 💬 7. 댓글 & 계층형 답글 API (`/comment`)

| 메서드 | URL | 권한 | 설명 | Request Body / Params | Response Data |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/comment/{postId}` | User | 댓글/답글 작성 | Path: `postId`, Body: `CommentDto` | `CommentResponseDto` |
| `GET` | `/comment/{postId}` | Public | 특정 게시글 댓글 목록 (계층형) | Path: `postId` | `List<CommentResponseDto>` |
| `PUT` | `/comment/update/{commentId}` | Author/Admin | 댓글 수정 | Path: `commentId`, Body: `CommentUpdateDto` | `CommentResponseDto` |
| `DELETE` | `/comment/{commentId}` | Author/Admin | 댓글 삭제 | Path: `commentId` | `null` |

<details>
<summary><b>📄 댓글 관련 DTO 상세</b></summary>

- **CommentDto** (작성 요청): `parentId` (Long, 최상위 댓글은 `null` / 답글일 경우 부모 댓글 ID), `content` (String)
- **CommentResponseDto** (응답): `id` (Long), `authorName` (String), `authorId` (Long), `authorEmail` (String), `content` (String), `createdAt` (LocalDateTime), `children` (`List<CommentResponseDto>` - 대댓글 리스트)
- **CommentUpdateDto** (수정 요청): `commentId` (Long), `content` (String)

</details>



